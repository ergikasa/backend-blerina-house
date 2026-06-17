package com.blerinahouse.controller.admin;

import com.blerinahouse.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/images")
@RequiredArgsConstructor
public class AdminImageController {

    private final ImageService imageService;

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long imageId) {
        imageService.delete(imageId);
    }

    @PutMapping("/{imageId}/cover")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setCover(@PathVariable Long imageId) {
        imageService.setCover(imageId);
    }
}