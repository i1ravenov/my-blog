package ru.yandex.practicum.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.model.Post;

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

        String sql = """
        insert into post (title, text, tags)
        values (?, ?, ?)
        returning id, title, text, tags
    """;

        String tagsJson;
        try {
            tagsJson = mapper.writeValueAsString(newPostDto.tags());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    try {
                        return new Post(
                                rs.getLong("id"),
                                rs.getString("title"),
                                rs.getString("text"),
                                mapper.readValue(
                                        rs.getString("tags"),
                                        new TypeReference<List<String>>() {}
                                ),
                                0,
                                0
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                newPostDto.title(),
                newPostDto.text(),
                tagsJson
        );
    }

}
