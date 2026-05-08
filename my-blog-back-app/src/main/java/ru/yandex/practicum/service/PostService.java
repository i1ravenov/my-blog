package ru.yandex.practicum.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.dto.UpdatePostDto;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PostService {

    public static final String UPLOAD_DIR = "/app/uploads/";

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post findById(long id) {
        return postRepository.findById(id);
    }

    public Post savePost(NewPostDto postDto) {
        return postRepository.save(postDto);
    }

    public void updateImage(long postId, MultipartFile image) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String fileName = postId + ".jpeg";
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        Files.write(filePath, image.getBytes());
    }

    public Resource download(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            byte[] content = Files.readAllBytes(filePath);

            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CommentDto> findAllCommentsForPost(long id) {
        return postRepository.findAllCommentsForPost(id);
    }

    public CommentDto saveComment(CommentDto commentDto) {
        return postRepository.saveComment(commentDto);
    }

    public void deleteComment(long postId, long commentId) {
        postRepository.deleteComment(postId, commentId);
    }

    public CommentDto updateComment(long postId, long commentId, String text) {
        return postRepository.updateComment(postId, commentId, text);
    }

    public Post updatePost(UpdatePostDto updatePostDto) {
        return postRepository.updatePost(updatePostDto);
    }

    public void deletePost(long postId) {
        postRepository.deletePost(postId);
    }
}
