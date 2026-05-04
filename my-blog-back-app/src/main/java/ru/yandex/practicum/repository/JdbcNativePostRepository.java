package ru.yandex.practicum.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
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

}
