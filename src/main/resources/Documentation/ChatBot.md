# OkaneTransfer — Chatbot Setup & Developer Guide

## Overview

The chatbot is a multi-layer support assistant for OkaneTransfer. It handles client questions about transfers, fees, OTP, withdrawals, and more. It uses semantic search (pgvector + Jina) to answer FAQ questions without calling the LLM, and falls back to Groq (Llama 3.1) only for novel queries.

---

## Architecture — Request Pipeline

Every message passes through these gates in order. Each gate can exit early.

```
User message
    │
    ├─ Gate 1: Input sanity        (blank / too long → reject, no DB write)
    ├─ Gate 2: Rate limiting       (> 30 msg/hour → reject, no DB write)
    │
    │  [save user message to DB]
    │
    ├─ Gate 3: Repetition guard    (same message twice in session → static reply)
    ├─ Gate 4: Escalation keywords (hard: always | soft: only after 3 messages)
    ├─ Gate 5: Semantic FAQ        (Jina embed → pgvector cosine similarity)
    │              ├─ hit + escalation flag → escalate
    │              └─ hit + answer → return FAQ answer (Groq never called)
    └─ Gate 6: Groq LLM            (fallback to keyword FAQ if Groq fails)
```

---

## Dependencies

| Service | Purpose | Free tier |
|---------|---------|-----------|
| Railway Postgres | DB + pgvector extension | ✅ |
| [Jina AI](https://jina.ai) | Text embeddings (1024 dims) | 1M tokens/month |
| [Groq](https://console.groq.com) | LLM responses (Llama 3.1) | generous free tier |

---

## First-time Setup

### 1. Enable pgvector on your Postgres instance

Run this once in your Railway Postgres query tab:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Verify:

```sql
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### 2. Configure `application.properties`

```properties
# Database
db.url=jdbc:postgresql://<host>:<port>/<db>
db.username=
db.password=
db.driver=org.postgresql.Driver

hibernate.ddl-auto=update

# Groq — https://console.groq.com/keys
groq.api.key=
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.model=llama-3.1-8b-instant
groq.timeout.seconds=8

# Jina — https://jina.ai (free, no card required)
jina.api.key=
jina.api.url=https://api.jina.ai/v1/embeddings
jina.model=jina-embeddings-v3

# Chatbot tuning
faq.similarity.threshold=0.78
chatbot.rate.limit.hourly=30
chatbot.max.message.length=500
chatbot.soft.escalation.min.messages=3
```

### 3. Start the app

Hibernate creates the `faq_embeddings` table automatically via `ddl-auto=update`. On first startup, `FaqDataInitializer` detects an empty table and seeds all FAQ entries from `src/main/resources/faq/*.md`. You will see:

```
FAQ seeding complete — 249 entries saved
```

Seeding only runs once. On subsequent restarts it logs `FAQ table already seeded — skipping`.

---

## FAQ Files

Located at `src/main/resources/faq/`. Each `.md` file covers one topic.

### Format

```markdown
QUESTION: Comment suivre mon transfert ?
ANSWER: Allez dans Historique et entrez votre code de transfert.

QUESTION: Mon transfert est-il arrivé ?
ANSWER: Le statut est visible dans la rubrique Suivi de votre espace client.

```

Rules:
- One blank line between entries
- `ANSWER:` must be on the same line as the answer text (single line)
- Keep answers under 200 characters — they go to the client unchanged
- Add `ESCALATE: true` on a line after `ANSWER:` to route that intent to a human agent instead of answering

```markdown
QUESTION: Mon argent a disparu de mon compte.
ANSWER: Nous allons escalader votre demande à un conseiller.
ESCALATE: true

```

### Re-seeding after edits

If you edit or add FAQ files, truncate the table and restart:

```sql
TRUNCATE TABLE faq_embeddings;
```

---

## Escalation

When a message triggers escalation, a `ChatEscalation` record is saved with a summary of the last 5 messages. An agent can review it from the admin dashboard.

**Hard triggers** — escalate immediately (message 1+):
`agent`, `humain`, `human`, `conseiller`

**Soft triggers** — escalate only after 3+ messages in the session:
`bloque`, `probleme`, `urgent`, `reclamation`, `plainte`, `fraude`, `disparu`, `arnaque`

**Semantic escalation** — any FAQ entry with `ESCALATE: true` matched above the similarity threshold also triggers escalation.

---

## Tuning

| Property | Default | Effect |
|----------|---------|--------|
| `faq.similarity.threshold` | `0.78` | Raise if FAQ returns wrong answers; lower if missing obvious matches |
| `chatbot.rate.limit.hourly` | `30` | Max user messages per hour before hard block |
| `chatbot.max.message.length` | `500` | Characters; longer messages are rejected before any DB write |
| `chatbot.soft.escalation.min.messages` | `3` | Session message count before soft keywords trigger escalation |

---

## Key Classes

| Class | Role |
|-------|------|
| `ChatbotServiceImpl` | Orchestrates the full pipeline |
| `SemanticFaqService` | Embeds query, runs cosine similarity, returns FAQ result |
| `EmbeddingService` | Calls Jina AI, returns pgvector-compatible string |
| `GroqChatServiceImpl` | Calls Groq LLM with conversation history |
| `FaqDataInitializer` | Seeds `faq_embeddings` table on first startup |
| `FaqEmbeddingRepository` | Native query with `<=>` cosine distance operator |



Here's the updated doc with a new section appended:

---

## Multilingual Support (Arabic, English, French)

The chatbot automatically detects the language of each incoming message using **Lingua** and responds in that language throughout the entire pipeline. No configuration is needed — detection is automatic.

**How it works:**

A `LanguageDetectorService` component (built once at startup) identifies the language via Lingua and returns an ISO 639-1 code (`ar`, `en`, `fr`). This code flows through every gate:

- **Gates 1–3** (sanity, rate limit, repetition): static replies are looked up from an `I18N` map with entries for `fr`, `en`, and `ar`.
- **Gate 4** (escalation): the confirmation message is also looked up from the same map.
- **Gate 5** (semantic FAQ): answers are stored in French in `faq_embeddings`. If the detected language is not French, the matched answer is passed through `GroqChatService.translate()` before being returned to the client.
- **Gate 6** (Groq LLM): the system prompt instructs the model to reply strictly in the detected language. The keyword-based fallback (`generateFaqReply`) also carries Arabic keywords and translates its French answers via `GroqChatService.translate()`.

To add a new language, add it to `LanguageDetectorService`'s `fromLanguages(...)` call, add entries to the `I18N` map in `ChatbotServiceImpl`, and add keywords to `generateFaqReply`. The Groq and translation layers require no changes.