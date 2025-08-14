package com.sku_likelion.Moving_Cash_back.scheduler;

import com.sku_likelion.Moving_Cash_back.enums.ActivityType;
import com.sku_likelion.Moving_Cash_back.enums.LevelType;
import com.sku_likelion.Moving_Cash_back.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeService challengeService;

    /**
     * 매일 00:00(KST)에 실행
     * cron = "초 분 시 일 월 요일"
     */

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void regenerateDailyChallenge(){
        challengeService.deleteAllChallenges();

        int perLevelCount = 5;

        for (ActivityType activity : List.of(ActivityType.WALKING, ActivityType.RUNNING)) {
            for (LevelType level : LevelType.values()) {
                challengeService.generateAndSave(activity, level, perLevelCount);
            }
        }
    }
}
