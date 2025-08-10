package com.sku_likelion.Moving_Cash_back.service;

import com.sku_likelion.Moving_Cash_back.domain.User;
import com.sku_likelion.Moving_Cash_back.dto.request.UserDTO;
import com.sku_likelion.Moving_Cash_back.exception.DuplicateUserException;
import com.sku_likelion.Moving_Cash_back.exception.InvalidPasswordException;
import com.sku_likelion.Moving_Cash_back.exception.InvalidUserException;
import com.sku_likelion.Moving_Cash_back.repository.UserRepository;
import com.sku_likelion.Moving_Cash_back.security.JwtUtility;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService{

    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private  final PasswordEncoder passwordEncoder;

    public User tokenToUser(String token){
        return userRepository.findByUserId(jwtUtility.getClaimsFromJwt(token).getSubject())
                .orElseThrow(() -> new InvalidUserException("존재하지 않는 사용자"));
    }

    @Transactional
    public User signUp(UserDTO.CreateUser req){
        if(userRepository.existsByUserId(req.getUserId())){
            throw new DuplicateUserException("이미 존재하는 사용자");
        }
        String encodedPassword = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getUserId(), encodedPassword);
        user.setName(req.getName());
        return userRepository.save(user);
    }

    public String login(UserDTO.Login req){
        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(()-> new InvalidUserException("존재하지 않는 사용자"));
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())){
            throw new InvalidPasswordException("비밀번호가 틀렸습니다.");
        }
        return jwtUtility.generateJwt(user.getUserId());
    }

    @Transactional
    public User updateUser(String token, String name){
        User user = tokenToUser(token);
        user.setName(name);
        return user;
    }

    @Transactional
    public void deleteUser(String token){
        User user = tokenToUser(token);
        userRepository.delete(user);
    }

    public List<User> getAllUsers(){return userRepository.findAll();}

    public User findByUserId(String userId){
        return userRepository.findByUserId(userId)
                .orElseThrow(()-> new InvalidUserException("해당 사용자 없음"));
    }

    public User findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new InvalidUserException("해당 사용자 없음"));
    }

    public List<User> findByName(String name){
        return userRepository.findByName(name);
    }

    public UserDetails loadUserByUserId(String userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new InvalidUserException("해당 사용자 없음"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
    }

}
