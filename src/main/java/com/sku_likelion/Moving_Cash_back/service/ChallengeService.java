package com.sku_likelion.Moving_Cash_back.service;


import com.sku_likelion.Moving_Cash_back.domain.Challenge;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.response.ChallengeDTO;
import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import com.sku_likelion.Moving_Cash_back.enums.ChallengeStatus;
import com.sku_likelion.Moving_Cash_back.enums.LevelType;
import com.sku_likelion.Moving_Cash_back.exception.InvalidChallengeException;
import com.sku_likelion.Moving_Cash_back.openai.OpenAIClient;
import com.sku_likelion.Moving_Cash_back.repository.ChallengeRepository;
import com.sku_likelion.Moving_Cash_back.repository.JoinChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final JoinChallengeRepository joinChallengeRepository;

    private static final Map<LevelType, int[]> STEPS = Map.of(
            LevelType.BEGINNER, new int[]{1000, 3000},
            LevelType.INTERMEDIATE, new int[]{7000, 10000},
            LevelType.ADVANCED, new int[]{12000, 20000}
    );

    private static final Map<LevelType, double[]> RUN_KM = Map.of(
            LevelType.BEGINNER, new double[]{1.0, 3.0},
            LevelType.INTERMEDIATE, new double[]{4.0, 6.0},
            LevelType.ADVANCED, new double[]{7.0, 10.0}
    );

    private static final Map<LevelType, Integer> REWARD_RANGE = Map.of(
            LevelType.BEGINNER, 300,
            LevelType.INTERMEDIATE, 500,
            LevelType.ADVANCED, 1000
    );

    @Transactional
    public List<Challenge> generateAndSave(ActivityType activity, LevelType level, int count){
        List<Challenge> list = new ArrayList<>();
        for(int i = 0; i < count; i++){
            Challenge ch = switch(activity){
                case WALKING -> createWalkChallenge(level);
                case RUNNING -> createRunChallenge(level);
                case PAUSE -> null;
                case END -> null;
            };
            list.add(ch);
        }
        return challengeRepository.saveAll(list);
    }

    private Challenge createWalkChallenge(LevelType level){
        int[] range = STEPS.get(level);
        int steps = randomInt(range[0], range[1]);
        steps = roundTo(steps, 100);
        long reward = pickReward(level);

        String title = String.format("%,d보 걷기", steps);

        return new Challenge(
                level, ActivityType.WALKING, title, reward
        );
    }

    private Challenge createRunChallenge(LevelType level) {
        double[] range = RUN_KM.get(level);
        double km = randomDouble(range[0], range[1]);  // 예: 4.2 km
        km = Math.round(km * 10) / 10.0;               // 한 자리 소수로 포맷

        int reward = pickReward(level);

        String title = String.format("%.1fkm 러닝", km);

        return new Challenge(
                level, ActivityType.RUNNING, title, (long) reward
        );
    }

    // 유틸 -------------

    private int pickReward(LevelType level){
        return REWARD_RANGE.get(level);
    }

    private int randomInt(int minInclusive, int maxInclusive){
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive+1);
    }

    private double randomDouble(double minInclusive, double maxInclusive) {
        return ThreadLocalRandom.current().nextDouble(minInclusive, maxInclusive);
    }

    // 정수값을 지정 단위로 반올림
    private int roundTo(int value, int unit) {
        return Math.round(value / (float) unit) * unit;
    }

    @Transactional
    public void deleteAllChallenges() {
        challengeRepository.deleteAll();
    }

    public List<ChallengeDTO.ChallengeRes> getAllChallenge(User user, LocalDate date){
        List<Challenge> challenges = challengeRepository.findByCreatedAt(date);
        if(challenges.isEmpty()){
            throw new InvalidChallengeException("해당 날짜의 챌린지가 없습니다.");
        }

        return challenges.stream()
                .map(ch -> new ChallengeDTO.ChallengeRes(
                        ch.getId(),
                        ch.getLevel(),
                        ch.getActivity(),
                        ch.getTitle(),
                        ch.getReward(),
                        joinChallengeRepository.existsByUserAndChallenge(user, ch)
                ))
                .toList();
    }
}
