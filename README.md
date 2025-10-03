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
Add the dependency to your application.

Maven:

```xml
<dependency>
  <groupId>it.trinex</groupId>
  <artifactId>s3m</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration
Configure your AWS credentials, region, and bucket under the `s3m` prefix.

application.yml:

```yaml
s3m:
  accessKeyId: YOUR_ACCESS_KEY_ID
  secretAccessKey: YOUR_SECRET_ACCESS_KEY
  s3:
    bucketName: your-bucket
    region: eu-central-1
    # Optional: prefix automatically applied by built‑in HTTP endpoints
    endpointsPrefix: "/direct/"
  # Optional: turn on built‑in endpoints
  autoendpoint:
    enabled: true
```

Notes:
- Startup validation: the auto‑configuration performs a `HeadBucket` call during startup. If the bucket is not accessible with the provided credentials/region, the application will fail to start with a clear error.
- The property `s3m.s3.endpointsEnabled` is present but not used by the auto‑configuration; use `s3m.autoendpoint.enabled` to enable the built‑in HTTP controller.


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
    // URL valid for 5 minutes; do not force the endpoints prefix
    return s3m.generateUploadUrl(key, 5);
  }

  public String createDownloadUrl(String key) {
    // URL valid for 10 minutes; force applying the configured prefix
    return s3m.generateDownloadUrl(key, 10, true);
  }
}
```

`S3MService` methods:
- `generateUploadUrl(key, expireMinutes)`
- `generateUploadUrl(key, expireMinutes, forcePrefix)`
- `generateDownloadUrl(key, expireMinutes)`
- `generateDownloadUrl(key, expireMinutes, forcePrefix)`

If `forcePrefix` is true, the configured `s3m.s3.endpointsPrefix` (default `"/direct/"`) is applied to the key (e.g., `"/direct/myfile.png"`).


### 2) Built‑in HTTP endpoints (optional)
Enable the controller by adding:

```yaml
s3m:
  autoendpoint:
    enabled: true
```

Available endpoints under `/api/s3m`:

- `GET /api/s3m/upload?key=myfile.png&expireMinutes=5`
- `GET /api/s3m/download?key=myfile.png&expireMinutes=5`

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


## How it works (under the hood)
- Auto‑configuration creates beans only if you haven’t defined your own.
- It builds `S3Client` and `S3Presigner` with static credentials from `s3m.*` properties.
- A startup check (`HeadBucket`) ensures your bucket is reachable early.
- `S3MService` wraps the `S3Presigner` to create the URLs.


## Troubleshooting
- Startup fails with `Unable to access S3 bucket`:
  - Verify `s3m.s3.bucketName` and `s3m.s3.region`.
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

