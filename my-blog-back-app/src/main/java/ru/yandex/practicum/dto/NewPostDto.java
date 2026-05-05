package ru.yandex.practicum.dto;

import java.util.List;

public record NewPostDto(String title, String text, List<String>tags) {
}
