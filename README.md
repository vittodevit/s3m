# s3m — Simple pre‑signed S3 URLs for Spring Boot

A tiny Spring Boot library that auto‑configures the AWS S3 SDK v2 and gives you:

- An `S3Client`
- An `S3Presigner`
- A small `S3MService` to generate pre‑signed upload/download URLs
- Optional HTTP endpoints to request pre‑signed URLs from your frontend


## Features
- Auto‑configured beans (no boilerplate): `S3Client`, `S3Presigner`, `S3MService`.
- Fail‑fast startup: validates access to the configured bucket.
- Simple service API for generating pre‑signed URLs.
- Optional REST endpoints at `/api/s3m` (opt‑in via config).


## Installation
Add the dependency to your `pom.xml`. Check that GitHub repository is available, 
instructions [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

```xml
<dependency>
  <groupId>it.trinex</groupId>
  <artifactId>s3m</artifactId>
  <version>0.0.4</version>
</dependency>
```

## Configuration
Configure your AWS credentials, endpoint, and bucket under the `s3m` prefix.

application.yml:

```yaml
s3m:
  accessKeyId: YOUR_ACCESS_KEY_ID
  secretAccessKey: YOUR_SECRET_ACCESS_KEY
  s3:
    bucketName: your-bucket
    endpoint: https://minio.or.r2.endpoint
    # Optional: region used for signing. Defaults to us-east-1 if omitted.
    # For Cloudflare R2 use: auto
    region: us-east-1
  # Optional: turn on built‑in endpoints to upload and download files
  autoendpoint: false # default is false
```

Notes:
- If you see "Unable to load region from any of the providers", set `s3m.s3.region` or rely on the default `us-east-1`.
- For Cloudflare R2 set region to `auto`.
- Startup validation: the auto‑configuration performs a `HeadBucket` call during startup. If the bucket is not accessible with the provided credentials/region, the application will fail to start with a clear error.


## Getting started
There are two ways to use the library: programmatically via `S3MService`, or via the optional HTTP endpoints.

### 1) Programmatic usage (recommended)
Inject `S3MService` wherever you need to generate pre‑signed URLs:

```java
import it.trinex.s3m.S3MService;
import org.springframework.stereotype.Service;

@Service
public class MyUploadService {
  private final S3MService s3m;

  public MyUploadService(S3MService s3m) {
    this.s3m = s3m;
  }

  public String createUploadUrl(String key) {
    // URL valid for 5 minutes;
    return s3m.generateUploadUrl(key, 5);
  }

  public String createDownloadUrl(String key) {
    // URL valid for 10 minutes;
    return s3m.generateDownloadUrl(key, 10);
  }
}
```

`S3MService` methods:
- `generateUploadUrl(key, expireMinutes)`
- `generateDownloadUrl(key, expireMinutes)`

### 2) Built‑in HTTP endpoints (optional)
Enable the controller by adding:

```yaml
s3m:
  autoendpoint: true
```

Available endpoints under `/api/s3m`:

- `GET /api/s3m/upload?key=myfile.png&expireMinutes=1`
- `GET /api/s3m/download?key=myfile.png&expireMinutes=1`

Both return a simple JSON body:

```json
{
  "url": "https://...",
  "key": "/direct/myfile.png"
}
```

Example cURL:

```bash
curl "http://localhost:8080/api/s3m/upload?key=avatar.png&expireMinutes=5"
```

Use the returned `url` to PUT the bytes directly to S3 from your client.


## Troubleshooting
- Startup fails with `Unable to access S3 bucket`:
  - Verify `s3m.s3.bucketName` and `s3m.s3.endpoint`.
  - Verify `s3m.accessKeyId` and `s3m.secretAccessKey` have permissions (e.g., `s3:PutObject`, `s3:GetObject`, `s3:ListBucket`).
  - Check for VPC endpoint or network restrictions affecting S3.
- URL immediately expires or is rejected:
  - Ensure your server/client clocks are reasonably in sync.
  - Increase `expireMinutes`.

## License
This library is released into the public domain under The Unlicense.

- License file: LICENSE (included in this repository)
- SPDX identifier: Unlicense
- More info: https://unlicense.org/

