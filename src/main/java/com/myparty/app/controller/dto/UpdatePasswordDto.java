package com.myparty.app.controller.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordDto(@NotNull String oldPassword, @NotNull String newPassword) { }
