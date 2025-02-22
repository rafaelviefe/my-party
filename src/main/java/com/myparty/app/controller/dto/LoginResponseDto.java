package com.myparty.app.controller.dto;

public record LoginResponseDto(String accessToken, Long expiresIn) {
}
