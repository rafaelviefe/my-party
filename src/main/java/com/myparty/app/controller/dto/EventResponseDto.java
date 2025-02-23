package com.myparty.app.controller.dto;

import java.time.Instant;

public record EventResponseDto(
		Long eventId,
		String title,
		String description,
		String location,
		Instant date,
		Double price,
		String category,
		Double rating,
		Long reviews,
		String organizerName
) {}
