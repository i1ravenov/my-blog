package ru.yandex.practicum.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.dto.UpdatePostDto;
import ru.yandex.practicum.model.Post;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class JdbcNativePostRepository implements PostRepository {
    private static ObjectMapper mapper = new ObjectMapper();
    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findAll() {
        return jdbcTemplate.query(
                "select id, title, text, tags, likes_count, comments_count from post",
                (rs, rowNum) -> {
                    try {
                        return new Post(
                                rs.getLong("id"),
                                rs.getString("title"),
                                rs.getString("text"),
                                mapper.readValue(rs.getString("tags"),
                                        mapper.getTypeFactory().constructCollectionType(List.class, String.class)),
                                rs.getInt("likes_count"),
                                rs.getInt("comments_count")
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Post findById(long id) {
        return jdbcTemplate.queryForObject(
                "select id, title, text, tags, likes_count, comments_count from post where id = ?",
                (rs, rowNum) -> {
                    try {
                        return new Post(
                                rs.getLong("id"),
                                rs.getString("title"),
                                rs.getString("text"),
                                mapper.readValue(
                                        rs.getString("tags"),
                                        mapper.getTypeFactory()
                                                .constructCollectionType(List.class, String.class)
                                ),
                                rs.getInt("likes_count"),
                                rs.getInt("comments_count")
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                id
        );
    }

    @Override
    public Post save(NewPostDto newPostDto) {
        String tagsJson;
        try {
            tagsJson = mapper.writeValueAsString(newPostDto.tags());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into post (title, text, tags) values (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, newPostDto.title());
            ps.setString(2, newPostDto.text());
            ps.setString(3, tagsJson);
            return ps;
        }, keyHolder);

        return findById(keyHolder.getKey().longValue());
    }

    @Override
    public List<CommentDto> findAllCommentsForPost(long id) {
        return jdbcTemplate.query(
                "select id, text, post_id from comment where post_id = ?",
                (rs, rowNum) -> new CommentDto(
                        rs.getLong("id"),
                        rs.getString("text"),
                        rs.getLong("post_id")
                ),
                id
        );
    }

    @Override
    public CommentDto saveComment(CommentDto commentDto) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into comment (text, post_id) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, commentDto.text());
            ps.setLong(2, commentDto.postId());
            return ps;
        }, keyHolder);
        return new CommentDto(keyHolder.getKey().longValue(), commentDto.text(), commentDto.postId());
    }

    @Override
    public void deleteComment(long postId, long commentId) {
        jdbcTemplate.update("delete from comment where id = ? and post_id = ?", commentId, postId);
    }

    @Override
    public CommentDto updateComment(long postId, long commentId, String text) {
        jdbcTemplate.update(
                "update comment set text = ? where id = ? and post_id = ?",
                text, commentId, postId
        );
        return new CommentDto(commentId, text, postId);
    }

    @Override
    public Post updatePost(UpdatePostDto updatePostDto) {
        String tagsAsJson;
        try {
            tagsAsJson = mapper.writeValueAsString(updatePostDto.tags());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        jdbcTemplate.update(
                "update post set title = ?, text = ?, tags = ?, likes_count = 0, comments_count = 0 where id = ?;",
                updatePostDto.title(),
                updatePostDto.text(),
                tagsAsJson,
                updatePostDto.id()
        );

        return jdbcTemplate.queryForObject(
                "select id, title, text, tags, likes_count, comments_count from post where id = ?",
                (rs, rowNum) -> {
                    try {
                        return new Post(
                                rs.getLong("id"),
                                rs.getString("title"),
                                rs.getString("text"),
                                mapper.readValue(
                                        rs.getString("tags"),
                                        mapper.getTypeFactory()
                                                .constructCollectionType(List.class, String.class)
                                ),
                                rs.getInt("likes_count"),
                                rs.getInt("comments_count")
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                updatePostDto.id()
        );
    }
}
