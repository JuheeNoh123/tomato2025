package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.Challenge;
import com.sku_likelion.Moving_Cash_back.domain.JoinChallenge;
import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.JoinChallengeDTO;
import com.sku_likelion.Moving_Cash_back.exception.InvalidChallengeException;
import com.sku_likelion.Moving_Cash_back.exception.InvalidUserException;
import com.sku_likelion.Moving_Cash_back.repository.ChallengeRepository;
import com.sku_likelion.Moving_Cash_back.repository.JoinChallengeRepository;
import com.sku_likelion.Moving_Cash_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinChallengeService {

    private final ChallengeRepository challengeRepository;
    private final JoinChallengeRepository joinChallengeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createJoinChallenge(User curretUser, JoinChallengeDTO.JoinChallengeReq req){
        User user = userRepository.findByUserId(curretUser.getUserId())
                .orElseThrow(()->new InvalidUserException("사용자를 찾을 수 없스니다."));

        Challenge challenge = challengeRepository.findById(req.getChallengeId())
                .orElseThrow(()-> new InvalidChallengeException("해당 챌린지가 없습니다."));

        try {
            joinChallengeRepository.save(new JoinChallenge(challenge, user));
        } catch (DataIntegrityViolationException e) {
            // uk_user_challenge 제약 위반 → 중복 참여
            throw new IllegalStateException("이미 참여한 챌린지입니다.", e);
        }

        user.setPoint(user.getPoint() + challenge.getReward());
    }
}
