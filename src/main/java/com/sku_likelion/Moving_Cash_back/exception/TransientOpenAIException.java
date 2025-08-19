package com.sku_likelion.Moving_Cash_back.exception;

//일시적 오류 : 429, 네트워크 타임아웃, 일시적 서버 다운
public class TransientOpenAIException extends RuntimeException{
    public TransientOpenAIException(String m) { super(m); }
}
