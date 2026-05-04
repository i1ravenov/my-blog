package ru.yandex.practicum.controller; // Класс находится в пакете с контроллерами

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.Page;
import ru.yandex.practicum.service.PostService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

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


    @GetMapping("/posts/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable long id) throws IOException {

        Path path = Path.of("images/" + id + ".jpg");

        Resource resource;

        if (Files.exists(path)) {
            resource = new UrlResource(path.toUri());
        } else {
            resource = new UrlResource(
                    URI.create("https://upload.wikimedia.org/wikipedia/commons/thumb/6/65/No-Image-Placeholder.svg/960px-No-Image-Placeholder.svg.png?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=thumbnail&_=20200912122019")
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}
