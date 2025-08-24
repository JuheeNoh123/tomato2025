package com.sku_likelion.Moving_Cash_back.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sku_likelion.Moving_Cash_back.config.external.openai.OpenAIProperties;
import com.sku_likelion.Moving_Cash_back.dto.response.MovingSpotDTO;
import com.sku_likelion.Moving_Cash_back.exception.PermanentOpenAIException;
import com.sku_likelion.Moving_Cash_back.exception.TransientOpenAIException;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceRequest;
import com.sku_likelion.Moving_Cash_back.kakao.dto.PlaceResponse;
import com.sku_likelion.Moving_Cash_back.openai.dto.*;
import com.sku_likelion.Moving_Cash_back.dto.request.MovingSpotDTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAIClient {

    private final ObjectMapper om; //JSON 직렬화/역직렬화
    private final WebClient openAIWebClient;
    private final OpenAIProperties props;

    public List<PlaceResponse> rerankPlace(List<PlaceResponse> places, int topK){
        if(places == null || places.isEmpty()) throw new IllegalArgumentException("후보지가 없습니다.");

        // 장소 후보 과다 방지 토큰 비용 down
        List<PlaceResponse> trimmed = places.stream().limit(50).toList();

        // OpenAI로 보낼 최소 정보만 추출
        List<Candidate> cands = trimmed.stream()
                .map(p -> new Candidate(
                        p.getId(),
                        p.getName(),
                        p.getAddress() == null ? "":p.getAddress(),
                        p.getCategory() == null ? "":p.getCategory()
                ))
                .toList();

        // id -> 원본 매핑
        Map<String, PlaceResponse> byId = trimmed.stream()
                .collect(Collectors.toMap(PlaceResponse::getId, p -> p, (a,b)->a));
        // topk 범위 설정
        int k = Math.max(1, Math.min(topK, 20));

        // 프롬프트
        String system = """
        당신은 지역 명소(POI: Point of Interest) 재랭킹 전문가입니다.
        주어진 후보 장소 목록을 기반으로, 필요하다면 웹 검색을 통해 대중적으로 알려지거나
        좋은 평가를 받은 곳을 파악하고 각 장소의 점수를 0~100 사이의 점수로 매기고
        웹 서칭을 통해 장소를 고른이유와 점수를 왜 그렇게 줬는지 자세하게 reason에 작성해,
        가장 높은 점수 순으로 반환하세요.
                                                                    
        반드시 다음 '정확한' JSON 배열만 반환해야 합니다(설명/마크다운 금지).
        형식: [{"id":"<원본ID>","score":0 ~ 100, "reason":"뽑은 이유"}, ...]
        예시: [{"id":"123","score":87, "reason":"<너가 만든 이유>"},{"id":"456","score":72,"reason":"<이게 왜 뽑혔는지에 대한 이유>"}]
        추가 설명이나 마크다운은 절대 포함하지 마세요.
        """;

        String candidatesJson;
        try{
            candidatesJson = om.writeValueAsString(cands);
        }catch (Exception e){
            throw new IllegalStateException("Failed to serialize candidates", e);
        }

        String user = """
        후보 장소 목록(필드: id, name, address, category):
        %s
                        
       작업: 위 후보들 중에서 일반 사용자 입장에서 가장 매력적인 상위 %d곳을 골라주세요.
       가능하다면 웹 검색을 참고하여 인기 있는 장소, 평판이 좋은 장소, 또는 이름/카테고리가
       특징적인 장소를 우선 선택하세요. 중복되거나 체인점/지점은 피하는 것이 좋습니다.
       웹 검색을 할때는 가능한 네이버만 참고하도록 하고, 가능한 한 사용자 리뷰와 블로그 리뷰가 많으면서
       별점이 높은곳을 위주로 상대적 점수를 매겨 추천해주세요.
       참고하는 url은 map.naver.com을 잘 참고하도록 해.
                        
       반드시 JSON 배열로만 결과를 반환하세요.
        """.formatted(candidatesJson, k);

        var req = new ResponseRequest(
                props.getModel(),                           // 반드시 Responses 지원 모델 (예: "gpt-4.1" / "gpt-4o")
                system + "\n\n" + user,                     // 간단히 합쳐서 input 사용
                List.of(Map.of("type", "web_search_preview")),
                Map.of("type", "web_search_preview"),       // 웹서치 강제 (원치 않으면 null)
                0.2
        );

//        String resp = openAIWebClient.post()
//                .uri("/responses")
//                .bodyValue(req)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, r ->
//                        r.bodyToMono(String.class)
//                                .flatMap(b-> Mono.error(new RuntimeException("OpenAI 4xx/5xx: "+b))))
//                .bodyToMono(String.class)
//                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
//                .block();

        String resp = openAIWebClient.post()
                .uri("/responses")
                .bodyValue(req)
                .retrieve()
                // 5xx → 재시도 대상
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.toEntity(String.class).flatMap(e -> {
                            var rid = e.getHeaders().getFirst("x-request-id"); // 요청 ID 로깅
                            var body = e.getBody();
                            return Mono.error(new TransientOpenAIException(
                                    "5xx from OpenAI, reqId=" + rid + ", body=" + body
                            ));
                        })
                )
                // 4xx → 즉시 실패
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new PermanentOpenAIException("OpenAI 4xx: " + b)))
                )
                .bodyToMono(String.class)
                // 네트워크/서버 오류 재시도 (최대 3회, 지수 백오프)
                .retryWhen(
                        //최대 3번까지 재시도, 첫번째 재시도는 1초 후에 (기본 지연시간 1초), 지수적 백오프 : 1초 -> 2초 -> 4초
                        Retry.backoff(3, Duration.ofSeconds(1))
                                //백오프 시간이 아무리 늘어나도 최대 8초까지만 기다림
                                .maxBackoff(Duration.ofSeconds(8))
                                //랜덤성 : 4초를 기다려야 하는 경우 실제로는 3.2초~4.8초 사이에서 랜덤하게 기다림
                                .jitter(0.2)
                                //어떤 오류에서 재시도할지 조건 지정
                                .filter(ex ->
                                        ex instanceof TransientOpenAIException ||
                                                ex instanceof java.util.concurrent.TimeoutException ||
                                                ex instanceof java.io.IOException
                                )
                )
                // 연산자 타임아웃(응답 대기) — Netty responseTimeout과 맞추기
                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .block();


        if (resp == null || resp.isBlank()) {
            throw new IllegalStateException("OpenAI 응답 비어있음(HTTP body null/blank)");
        }

        String out;
        try {
            var root = om.readTree(resp);

            var ot = root.path("output_text");
            if (!ot.isMissingNode() && !ot.isNull() && !ot.asText().isBlank()) {
                out = ot.asText();
            } else {
                out = null;
                var output = root.path("output");
                if (output.isArray()) {
                    for (var item : output) {
                        if ("message".equals(item.path("type").asText())) {
                            var content = item.path("content");
                            if (content.isArray() && content.size() > 0) {
                                var first = content.get(0);
                                // "output_text" 또는 "text" 타입 모두 케어
                                var t = first.path("text").asText("");
                                if (!t.isBlank()) { out = t; break; }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 응답 JSON 파싱 실패(resp): " + resp, e);
        }
        if (out == null || out.isBlank()) {
            throw new IllegalStateException("OpenAI 응답 비어있음(파싱 실패). resp=" + resp);
        }

        String text = out.trim();
        if (text.startsWith("```")) {
            int i = out.indexOf('\n');
            int j = out.lastIndexOf("```");
            if (i >= 0 && j > i) {
                out = out.substring(i+1, j).trim();
            }
        }
        String cleaned = sanitizeJsonArray(out);
        // 응답
        record Ranked(String id, Integer score, String reason){}
        List<Ranked> rankedList = null;
        try{
            rankedList = om.readValue(cleaned, new TypeReference<List<Ranked>>() {});
        }catch (Exception e){
            throw new IllegalStateException("OpenAI 응답 Json 파싱 실패: " + cleaned, e);
        }

        // id 순서대로 원본 PlaceResponse 매핑
        List<PlaceResponse> ranked = new ArrayList<>();
        if (rankedList != null && !rankedList.isEmpty()) {
            // 점수 정규화/클리핑 및 정렬 보정
            rankedList.stream()
                    .filter(r -> r.id()!=null && byId.containsKey(r.id()))
                    .sorted((a,b) -> Double.compare(
                            b.score()==null?0.0:b.score(),
                            a.score()==null?0.0:a.score()))
                    .limit(k)
                    .forEach(r -> {
                        PlaceResponse p = byId.get(r.id());
                        System.out.println(r.reason);
                        // score 필드가 있다면 세팅
                        try { p.setScore(Math.max(0, Math.min(100, r.score() == null ? 0 : r.score()))); } catch(Exception ignored){}
                        ranked.add(p);
                    });

            // 부족하면 나머지로 채우기
            if (ranked.size() < k) {
                for (PlaceResponse p : trimmed) {
                    if (ranked.stream().noneMatch(x -> x.getId().equals(p.getId()))) {
                        try { p.setScore(0); } catch(Exception ignored){}
                        ranked.add(p);
                        if (ranked.size() >= k) break;
                    }
                }
            }
        }

        return ranked;
    }

    //추천 경로 만들기
    public MovingSpotDTO.WalkCourseRes generateWalkCourses(List<MovingSpotDTO.RecommendRes> candidates, WalkPref pref){
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("코스 후보가 없습니다(캐시 없음).");
        }

        String candsJson;
        try{
            candsJson = om.writeValueAsString(candidates);
        }catch (Exception e){
            throw new IllegalStateException("직렬화 실패",e);
        }

        String  system = """
    당신은 산책 코스 플래너입니다. 사용자의 조건에 맞춰 주어진 후보 장소들을 이어 1개의 산책 코스를 설계하세요.
    결과는 무조건 JSON만 반환하세요. 코드펜스/설명 금지.
    
    스키마:
     {
       "routeId":1     //1씩 증가
       "waypoints": [{"name":"...","lat":...,"lng":...}],
       "destination": {"name":"...", "lat":..., "lng":...}
     }
     //주의 : start(출발지)는 생성하지 말 것. 서버에서 주입함.
     
    규칙:
    - 좌표(lat,lng)는 입력 후보의 값을 그대로 사용.
    - waypoints는 '추천장소' + '주변POI(공원/산책로/볼거리)' 풀에서 선택.
    - destination은 의미있는 종착지(예: 넓은 공원, 산책 마무리하기 좋은 명소)로 선택.
    - 불필요한 왕복/되돌아감 최소화.
    - waypoints는 1~5개.
    - JSON 외 어떠한 텍스트/마크다운도 금지.
    """;

        String themes = joinOrNA(pref.getTheme());      // 예: "힐링, 반려동물, 야경" 또는 "없음"
        String difficulties = joinOrNA(pref.getDifficulty()); // 예: "쉬움, 보통" 또는 "없음"
        String conditions = joinOrNA(pref.getCondition());  // 예: "비 안 옴" 또는 "없음"

        String user = """
    사용자 조건:
    - 테마: %s
    - 난이도: %s
    - 추가조건: %s

    후보 풀(추천장소 + 주변POI):
    %s

    작업:
    - 조건에 가장 잘 맞는 코스를 1개 생성.
    - waypoints/destination 각각을 후보 중에서 선택
    - 난이도에 따라 각 장소 좌표를 확인해서 거리도 계산해서 난이도에 맞게 알아서 경로를 잘 짤것.
    - 불필요한 왕복이나 되돌아 가는 경로는 최소화할 것.
    - 예를 들어 테마가 반려동물 동반이라면 각 장소정보를 확인하여 반려동물 동반이 가능한 장소를 뽑아야함.
    """.formatted(
            nvl(themes), nvl(difficulties), nvl(conditions), candsJson
        );

        var req = new ResponseRequest(
                props.getModel(),
                system + "\n\n" + user,
                null,
                null,
                0.2
        );

        String resp = openAIWebClient.post()
                .uri("/responses")
                .bodyValue(req)
                .retrieve()
                // 5xx → 재시도 대상
                .onStatus(HttpStatusCode::is5xxServerError, r ->
                        r.toEntity(String.class).flatMap(e -> {
                            var rid = e.getHeaders().getFirst("x-request-id"); // 요청 ID 로깅
                            var body = e.getBody();
                            return Mono.error(new TransientOpenAIException(
                                    "5xx from OpenAI, reqId=" + rid + ", body=" + body
                            ));
                        })
                )
                // 4xx → 즉시 실패
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new PermanentOpenAIException("OpenAI 4xx: " + b)))
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .block();

        if (resp == null || resp.isBlank()){
            throw new IllegalStateException("OpenAI 응답 비어있음");
        }

        String out;
        try {
            var root = om.readTree(resp);

            var ot = root.path("output_text");
            if (!ot.isMissingNode() && !ot.isNull() && !ot.asText().isBlank()) {
                out = ot.asText();
            } else {
                out = null;
                var output = root.path("output");
                if (output.isArray()) {
                    for (var item : output) {
                        if ("message".equals(item.path("type").asText())) {
                            var content = item.path("content");
                            if (content.isArray() && content.size() > 0) {
                                var first = content.get(0);
                                // "output_text" 또는 "text" 타입 모두 케어
                                var t = first.path("text").asText("");
                                if (!t.isBlank()) { out = t; break; }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI 응답 JSON 파싱 실패(resp): " + resp, e);
        }
        if (out == null || out.isBlank()) {
            throw new IllegalStateException("OpenAI 응답 비어있음(파싱 실패). resp=" + resp);
        }

        String cleaned = sanitizeJsonArray(out);

        try {
            MovingSpotDTO.WalkCourseRes courses = om.readValue(cleaned, new TypeReference<MovingSpotDTO.WalkCourseRes>() {});

            BigDecimal curLat = pref.getLat();
            BigDecimal curLng = pref.getLng();

            MovingSpotDTO.Node start = new MovingSpotDTO.Node();
            start.setName("출발지");
            start.setLat(curLat);
            start.setLng(curLng);
            courses.setStart(start);

            return courses;

        } catch (Exception e) {
            throw new IllegalStateException("코스 JSON 파싱 실패: " + cleaned, e);
        }

    }

    // 문자열 조인
    private static String joinOrNA(List<String> list) {
        if (list == null || list.isEmpty()) return "없음";
        return list.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static String nvl(String s){ return (s==null || s.isBlank()) ? "미지정" : s; }


    /**
     * 모델이 코드펜스(``` 또는 ```json)로 감싸거나 앞뒤에 설명을 붙여도
     * JSON 배열만 깔끔히 추출해주는 정리 함수.
     */
    private static String sanitizeJsonArray(String s) {
        if (s == null) return "[]";
        String t = s.trim();

        // 코드펜스 제거 (```json, ``` 등)
        // 앞쪽 펜스
        if (t.startsWith("```")) {
            // 첫 줄(펜스 라인) 날리고 나머지
            int nl = t.indexOf('\n');
            if (nl >= 0) t = t.substring(nl + 1);
        }
        // 뒤쪽 펜스
        if (t.endsWith("```")) {
            t = t.substring(0, t.lastIndexOf("```")).trim();
        }

        // 혹시 남아있을 수 있는 백틱 문자 전부 제거 (예외적으로 남는 경우 방어)
        // (필수는 아니지만 안전망)
        if (t.indexOf('`') >= 0) {
            t = t.replace("```json", "")
                    .replace("```", "")
                    .trim();
        }

        // 내용 중에서 첫 '[' 과 마지막 ']' 사이만 추출 (앞뒤 설명/텍스트 제거)
        int l = t.indexOf('[');
        int r = t.lastIndexOf(']');
        if (l >= 0 && r >= 0 && r > l) {
            t = t.substring(l, r + 1).trim();
        }

        return t;
    }


}
