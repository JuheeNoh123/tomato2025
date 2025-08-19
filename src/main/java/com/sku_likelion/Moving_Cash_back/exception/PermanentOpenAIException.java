package com.sku_likelion.Moving_Cash_back.exception;

// 영구적 오류 : 잘못된 API 키, 요청 포맷이 잘못됨, 존재하지 않는 모델 호출
public class PermanentOpenAIException extends RuntimeException{
    public PermanentOpenAIException(String m) { super(m); }
}
