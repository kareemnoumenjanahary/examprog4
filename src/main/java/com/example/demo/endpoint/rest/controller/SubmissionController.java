@"
        package com.example.demo.endpoint.rest.controller;

import static java.io.File.createTempFile;

import com.example.demo.endpoint.rest.model.Submission;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.repository.SubmissionRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class SubmissionController {

    private final SubmissionRepository submissionRepository;
    private final BucketComponent bucketComponent;

    @PostMapping(value = "/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public ResponseEntity<Submission> createSubmission(
            @RequestParam("file") MultipartFile file, @RequestParam("email") String email) {

        var entity = new com.example.demo.repository.model.Submission();
        entity.setId(UUID.randomUUID().toString());
        entity.setEmail(email);
        entity.setThumbnailKey(null);
        entity.setCreatedAt(Instant.now());
        entity = submissionRepository.save(entity);

        var rawKey = "raw/" + entity.getId();
        var tmp = createTempFile("submission-" + entity.getId(), extensionOf(file));
        file.transferTo(tmp);
        bucketComponent.upload(tmp, rawKey);

        // TODO: publier ThumbnailGenerationRequested une fois EventProducer confirmé

        var body =
                Submission.builder()
                        .id(java.util.UUID.fromString(entity.getId()))
                        .email(entity.getEmail())
                        .thumbnailKey(null)
                        .createdAt(entity.getCreatedAt())
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/submissions")
    public java.util.List<Submission> listSubmissions() {
        return submissionRepository.findAll().stream()
                .map(
                        e ->
                                Submission.builder()
                                        .id(java.util.UUID.fromString(e.getId()))
                                        .email(e.getEmail())
                                        .thumbnailKey(e.getThumbnailKey())
                                        .createdAt(e.getCreatedAt())
                                        .build())
                .toList();
    }

    private String extensionOf(MultipartFile file) {
        var name = file.getOriginalFilename();
        var dot = name == null ? -1 : name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".tmp";
    }
}
"@ | Out-File -Encoding utf8 "src\main\java\com\example\demo\endpoint\rest\controller\SubmissionController.java"