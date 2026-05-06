package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.dto.UpdatePostDto;
import ru.yandex.practicum.model.Page;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.service.PostService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    @ResponseBody
    public Page getPage(@RequestParam("search") String search,
                        @RequestParam("pageNumber") int pageNumber,
                        @RequestParam("pageSize") int pageSize) {
        return new Page(
                postService.findAll(),
                false,
                false,
                1
        );
    }

    @ResponseBody
    @PostMapping("/posts")
    public Post createNewPost(@RequestBody NewPostDto newPostDto) {
        return postService.savePost(newPostDto);
    }

    @GetMapping("/posts/{id}")
    @ResponseBody
    public Post getPost(@PathVariable long id) {
        return postService.findById(id);
    }

    @GetMapping("/posts/{id}/comments")
    public List<CommentDto> getCommentsForPost(@PathVariable String id) {
        try {
            return postService.findAllCommentsForPost(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return List.of();
        }
    }

    @GetMapping("/posts/{id}/image")
    public ResponseEntity<Resource> getPostImage(@PathVariable long id) throws IOException {

        Path imagePath = Paths.get(PostService.UPLOAD_DIR + id + ".jpeg");

        Resource resource;

        if (Files.exists(imagePath)) {
            resource = new UrlResource(imagePath.toUri());
        } else {
            resource = new ClassPathResource("static/default.jpeg");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }

    @PutMapping("/posts/{id}/image")
    public ResponseEntity<Void> updatePostImage(
            @PathVariable long id,
            @RequestParam("image") MultipartFile image) throws IOException {

        postService.updateImage(id, image);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/comments")
    public CommentDto addComment(@PathVariable long id, @RequestBody CommentDto commentDto) {
        return postService.saveComment(new CommentDto(0, commentDto.text(), id));
    }

    @DeleteMapping("/posts/{postId}/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable long postId, @PathVariable long id) {
        postService.deleteComment(postId, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/posts/{postId}/comments/{id}")
    public CommentDto updateComment(
            @PathVariable long postId,
            @PathVariable long id,
            @RequestBody CommentDto commentDto) {
        return postService.updateComment(postId, id, commentDto.text());
    }

    @PutMapping("/posts/{id}")
    public Post updatePost(@RequestBody UpdatePostDto updatePostDto) {
        return postService.updatePost(updatePostDto);
    }
}
