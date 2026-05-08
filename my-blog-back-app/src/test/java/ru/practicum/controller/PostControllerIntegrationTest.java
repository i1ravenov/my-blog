package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.WebConfiguration;
import ru.practicum.configuration.DataSourceConfiguration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(classes = {
        DataSourceConfiguration.class,
        WebConfiguration.class,
})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        jdbcTemplate.execute("DELETE FROM comment");
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.execute("""
                INSERT INTO post (id, title, text, tags, likes_count, comments_count)
                VALUES (1, 'First Post', 'This is my first post', '["java","spring"]', 10, 2)
                """);
        jdbcTemplate.execute("""
                INSERT INTO post (id, title, text, tags, likes_count, comments_count)
                VALUES (2, 'Second Post', 'Learning Spring is fun', '["spring","backend"]', 25, 5)
                """);
        jdbcTemplate.execute("""
                INSERT INTO post (id, title, text, tags, likes_count, comments_count)
                VALUES (3, 'Third Post', 'Microservices guide', '["microservices","architecture"]', 40, 8)
                """);
        jdbcTemplate.execute("INSERT INTO comment (id, text, post_id) VALUES (1, 'Комментарий к посту 1', 1)");
        jdbcTemplate.execute("INSERT INTO comment (id, text, post_id) VALUES (2, 'Ещё один комментарий к посту 1', 1)");
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 4");
        jdbcTemplate.execute("ALTER TABLE comment ALTER COLUMN id RESTART WITH 3");
    }

    @Test
    void getPosts_returnsPageWithAllPosts() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(3)))
                .andExpect(jsonPath("$.posts[0].title").value("First Post"));
    }

    @Test
    void getPost_returnsPost() throws Exception {
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("First Post"))
                .andExpect(jsonPath("$.text").value("This is my first post"));
    }

    @Test
    void createPost_persistsAndReturns() throws Exception {
        String json = """
                {"title":"New Post","text":"New content","tags":["test"]}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("New Post"));

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(4)));
    }

    @Test
    void updatePost_changesFields() throws Exception {
        String json = """
                {"id":1,"title":"Updated Title","text":"Updated text","tags":["updated"]}
                """;

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    void deletePost_removesFromDb() throws Exception {
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(2)));
    }

    @Test
    void getComments_returnsAllForPost() throws Exception {
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Комментарий к посту 1"));
    }

    @Test
    void addComment_persistsAndReturns() throws Exception {
        String json = """
                {"id":0,"text":"Новый комментарий","postId":1}
                """;

        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Новый комментарий"))
                .andExpect(jsonPath("$.postId").value(1));

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void deleteComment_removesFromPost() throws Exception {
        mockMvc.perform(delete("/api/posts/1/comments/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void updateComment_changesText() throws Exception {
        String json = """
                {"id":1,"text":"Обновлённый текст","postId":1}
                """;

        mockMvc.perform(put("/api/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Обновлённый текст"));
    }

    @Test
    void getPostImage_returnsJpeg() throws Exception {
        mockMvc.perform(get("/api/posts/1/image"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }
}
