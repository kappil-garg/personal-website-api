# personal-website-api
[![Frontend](https://img.shields.io/badge/Frontend-Repository-0A66C2?logo=github)](https://github.com/kappil-garg/personal-website-ui)
&nbsp;
[![Live Website](https://img.shields.io/badge/Live-Website-teal?logo=google-chrome)](https://kappilgarg.dev/)

Spring Boot 3.5 / Java 21 REST API backend powering a personal portfolio website with optional AI capabilities.

---

## Key Highlight

Embeddings (RAG) are **disabled by default**.

### Why?

- Portfolio project, not production
- Embeddings require paid API usage
- Free tier (~100 requests/day) is quickly exhausted during re-indexing

### Result:

- Zero cost by default
- No dependency on Postgres/pgvector
- AI chat works using lightweight context (no vector search)

👉 You can enable full RAG anytime if needed.

---

## Free-tier API limitations

- `gemini-embedding-001` on the free tier allows **100 requests/day**.
- A typical portfolio (80–120 chunks) nearly exhausts this in a single re-index.

```properties
app.features.embeddings.index-batch-size=20        # chunks per API call
app.features.embeddings.index-batch-delay-ms=1000  # delay between batches
```

When the quota is hit:

- No failures
- Automatic fallback to context mode
- Only difference: no vector-based citations

---

## Re-indexing (admin)

```
POST /api/admin/ai/reindex-portfolio                 → 202 Accepted { jobId }
GET  /api/admin/ai/reindex-portfolio/status/{jobId}  → job progress
```

- Requires HTTP Basic auth
- Triggered on startup and blog updates

---
