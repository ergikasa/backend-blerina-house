# 🎬 VideoAD — Image-to-Video Ad Generator

> Turn a set of product or brand images into a polished video ad in seconds — no AI, no subscriptions, no cloud dependency. Just FFmpeg doing what it does best.

---

## What is this?

**VideoAD** is a full-stack MVP web application that converts a batch of images into a short video slideshow, ready to use as a social media ad or promotional clip.

You upload 3–10 images, pick a visual style, hit **Generate** — and the backend renders a proper `.mp4` file using FFmpeg and returns it for download.

No AI. No payments. No database. No vendor lock-in. Pure open-source tooling on your own machine.

---

## Demo Flow

```
Upload images → Choose style → Generate → Download MP4
```

| Step | What happens |
|------|--------------|
| 1. Upload | Drag & drop 3–10 images (JPG / PNG / WEBP) |
| 2. Style | Pick one of 3 predefined visual templates |
| 3. Generate | Frontend sends images to Spring Boot backend via multipart POST |
| 4. Render | Backend runs FFmpeg as a subprocess and returns the MP4 |
| 5. Download | Video plays in-browser; one-click download |

---

## Video Styles / Templates

### 🎬 Cinematic
- Slow Ken Burns zoom effect per image (`zoompan` filter)
- Smooth crossfade transitions between images
- ~4 seconds per image — ideal for brand storytelling
- Output: ~10–15 seconds for 3–4 images

### ⚡ Fast Ad
- Hard cuts with quick wipe transitions (left / right / up / down)
- ~2 seconds per image — high energy, social-media pacing
- Output: ~6–20 seconds depending on image count

### ◻ Minimal
- Clean fade between images, no zoom or motion
- ~3 seconds per image — clean and professional
- Output: ~9–30 seconds depending on image count

All templates output **1920×1080 (Full HD)** H.264 MP4, web-optimized with `faststart`.

---

## Tech Stack

### Frontend
- **React 18** (Vite)
- **Tailwind CSS** — dark industrial theme, Bebas Neue + DM Sans fonts
- **Axios** — multipart file upload with blob response handling

### Backend
- **Spring Boot 3.2** (Java 17)
- **No database** — all file I/O is in-process using `java.nio.file` temp directories
- **FFmpeg** — invoked via `ProcessBuilder` with fully parameterized commands
- **Automatic cleanup** — temp working directories deleted after each render

---

## Project Structure

```
VideoAD/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/videoad/
│       ├── VideoAdApplication.java         ← Spring Boot entry point
│       ├── controller/
│       │   └── VideoController.java        ← POST /api/generate-video
│       └── service/
│           ├── VideoService.java           ← Orchestrates rendering pipeline
│           └── FFmpegTemplates.java        ← 3 FFmpeg command builders
│
└── frontend/
    ├── package.json
    ├── vite.config.js                      ← Proxy /api → localhost:8080
    ├── tailwind.config.js
    └── src/
        ├── App.jsx                         ← State management & layout
        ├── index.css
        └── components/
            ├── UploadComponent.jsx         ← Drag & drop + image grid preview
            ├── StyleSelector.jsx           ← 3-card template picker
            ├── GenerateButton.jsx          ← Submit + loading state
            └── ResultViewer.jsx            ← Video player + download button
```

---

## API

### `POST /api/generate-video`

| Field | Type | Description |
|-------|------|-------------|
| `images` | `MultipartFile[]` | 3–10 image files |
| `style` | `String` | `cinematic` \| `fast` \| `minimal` |

**Response:** `video/mp4` binary stream

```bash
# Example with curl
curl -X POST http://localhost:8080/api/generate-video \
  -F "images=@img1.jpg" \
  -F "images=@img2.jpg" \
  -F "images=@img3.jpg" \
  -F "style=cinematic" \
  --output result.mp4
```

---

## Getting Started

### Prerequisites

```bash
# macOS
brew install ffmpeg maven

# Ubuntu / Debian
sudo apt install ffmpeg maven

# Windows
# Download ffmpeg: https://ffmpeg.org/download.html → add ffmpeg/bin to PATH
# Download Maven: https://maven.apache.org/download.cgi
```

Verify:
```bash
ffmpeg -version    # must work
java --version     # must be 17+
mvn --version      # must be 3.x
node --version     # must be 18+
```

### Run the Backend

```bash
cd backend
mvn spring-boot:run
```

Wait for: `Started VideoAdApplication on port 8080`

### Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

The Vite dev server proxies `/api` requests to `http://localhost:8080` automatically — no CORS configuration needed in development.

---

## How FFmpeg Works Here

Each template is a method in `FFmpegTemplates.java` that constructs a `ProcessBuilder` with a `filter_complex` chain.

**Example — Cinematic (simplified):**
```
ffmpeg
  -loop 1 -t 4 -i img_000.jpg
  -loop 1 -t 4 -i img_001.jpg
  -loop 1 -t 4 -i img_002.jpg
  -filter_complex "
    [0:v] scale=1920:1080, zoompan=z='min(zoom+0.0015,1.1)':d=100 [v0];
    [1:v] scale=1920:1080, zoompan=... [v1];
    [v0][v1] xfade=transition=fade:duration=1:offset=3 [vout]
  "
  -map [vout] -c:v libx264 -pix_fmt yuv420p output.mp4
```

**Rendering time** depends on your machine and number of images — typically **15–60 seconds** for 3–5 images. The frontend shows a loading state while the server processes.

---

## Limitations (MVP scope)

- No audio track support (video only)
- No text overlays or captions
- Single fixed output resolution (1920×1080)
- No job queue — one render at a time per server instance
- Temp files are cleaned up per-request (no history / gallery)

---

## Contributing

PRs welcome. Potential areas to extend:

- Add audio/music track upload
- Add text overlay support via FFmpeg `drawtext`
- Add output resolution selector (1080p / 720p / vertical 9:16)
- Add a job queue (e.g. Redis + Spring Batch) for concurrent renders
- Dockerize backend for easier deployment

---

## License

MIT — use it, fork it, ship it.
