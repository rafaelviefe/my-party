package com.myparty.app.controller.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
