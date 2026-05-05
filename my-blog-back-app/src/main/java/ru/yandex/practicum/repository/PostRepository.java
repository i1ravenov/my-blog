package ru.yandex.practicum.repository;

import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.model.Post;

import java.util.List;

public interface PostRepository {
    List<Post> findAll();

    Post findById(long id);

    Post save(NewPostDto newPostDto);
}
