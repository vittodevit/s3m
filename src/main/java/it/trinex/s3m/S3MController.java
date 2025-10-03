package it.trinex.s3m;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal REST controller exposing pre-signed S3 URL endpoints.
 *
 * <p>Base path: <code>/api/s3m</code>
 *
 * <p>Endpoints are only created when the property
 * <code>s3m.autoendpoint=true</code> is set. This makes it easy to enable the
 * built-in endpoints for quick prototypes and disable them in production.
 */
@RestController
@ConditionalOnProperty(prefix = "s3m", name = "autoendpoint", havingValue = "true")
@RequestMapping("/api/s3m")
@RequiredArgsConstructor
public class S3MController {
    private final S3MService service;

    /**
     * Generate a pre-signed URL to upload an object with HTTP PUT.
     *
     * <p>Example request:
     * GET /api/s3m/upload?key=myfile.png&expireMinutes=1
     *
     * @param key the object key to upload.
     * @param expireMinutes link validity in minutes (default 1, minimum 1)
     * @return a JSON payload containing the signed URL and the key
     */
    @GetMapping("/upload")
    public ResponseEntity<?> upload(
        @RequestParam("key") String key,
        @RequestParam(value = "expireMinutes", required = false, defaultValue = "1") int expireMinutes
    ) {
        String url = service.generateUploadUrl(key, expireMinutes);
        return ResponseEntity.ok(new SignedUrlResponse(url, key));
    }


    /**
     * Generate a pre-signed URL to download an object with HTTP GET.
     *
     * <p>Example request:
     * GET /api/s3m/download?key=myfile.png&expireMinutes=1
     *
     * @param key the object key to download.
     * @param expireMinutes link validity in minutes (default 1, minimum 1)
     * @return a JSON payload containing the signed URL and the key
     */
    @GetMapping("/download")
    public ResponseEntity<?> download(
        @RequestParam("key") String key,
        @RequestParam(value = "expireMinutes", required = false, defaultValue = "1") int expireMinutes
    ) {
        String url = service.generateDownloadUrl(key, expireMinutes);
        return ResponseEntity.ok(new SignedUrlResponse(url, key));
    }

    public record SignedUrlResponse(String url, String key) {}
}