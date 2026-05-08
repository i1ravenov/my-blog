package ru.yandex.practicum.repository;

import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.dto.UpdatePostDto;
import ru.yandex.practicum.model.Post;

import java.util.List;

public interface PostRepository {
    List<Post> findAll();

    Post findById(long id);

    Post save(NewPostDto newPostDto);

    List<CommentDto> findAllCommentsForPost(long id);

    CommentDto saveComment(CommentDto commentDto);

    void deleteComment(long postId, long commentId);

    CommentDto updateComment(long postId, long commentId, String text);

    Post updatePost(UpdatePostDto updatePostDto);

    void deletePost(long postId);
}
