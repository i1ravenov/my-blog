package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.NewPostDto;
import ru.yandex.practicum.model.Page;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.service.PostService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts") // Принимаем GET-запрос по адресу /home
    @ResponseBody        // Указываем, что возвращаемое значение является ответом
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

    @PostMapping("/posts")
    @ResponseBody
    public Post createNewPost(@RequestBody NewPostDto newPostDto, @RequestParam("image") MultipartFile image) throws IOException {
        Post result = postService.savePost(newPostDto);
        postService.updateImage(result.getId(), image);
        return result;
    }

    @GetMapping("/posts/{id}")
    @ResponseBody
    public Post getPost(@PathVariable long id) {
        return postService.findById(id);
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
}
