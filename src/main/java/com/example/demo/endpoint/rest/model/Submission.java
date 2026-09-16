@"
        package com.example.demo.endpoint.rest.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Submission {
    private UUID id;
    private String email;
    private String thumbnailKey;
    private Instant createdAt;
}
"@ | Out-File -Encoding utf8 "src\main\java\com\example\demo\endpoint\rest\model\Submission.java"