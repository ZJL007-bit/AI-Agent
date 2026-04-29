# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Backend (Spring Boot 3.4.5 + Java 21, listens on 8102 with /api context path)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Frontend (Vue 3, listens on 8081, proxies /api → localhost:8102)
cd len-ai-agent-frontend && npm run serve

# Frontend production build
cd len-ai-agent-frontend && npm run build

# Infrastructure: PostgreSQL with pgvector for vector storage
docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d pgvector/pgvector:pg16
```

API docs: http://localhost:8102/api/swagger-ui.html (Knife4j)

## Architecture

### Agent hierarchy (ReAct pattern)

`BaseAgent` → `ReActAgent` (think/act loop) → `ToolCallAgent` → concrete agents

- **`BaseAgent`** — state machine (`IDLE → RUNNING → FINISHED/ERROR`), step loop with max steps, stuck-detection, SSE streaming via `SseEmitter`
- **`ReActAgent`** — adds `think()` + `act()` abstraction over the base step loop
- **`ToolCallAgent`** — implements think/act via Spring AI `ToolCallingManager` + DashScope chat; `think()` calls the LLM and returns whether tools are requested; `act()` executes them

Concrete agents extending `ToolCallAgent`:
- **`LenManus`** — general-purpose "super agent" with all tools (web search, file ops, terminal, PDF/HTML generation, etc.), max 20 steps
- **`LenHealthAssistant`** — health advice domain agent, max 15 steps, with medical-appropriate system prompts

### Workflow-based agent (Coffee Shop) — different architecture

Located under `agent/coffee/`. Uses a graph-based workflow instead of ReAct:

- **`AgentGraphConfig`** orchestrates nodes via a routing map: `preprocessNode → intentClassifier → [productDialog | orderDialog | chitchat | transferToHuman]`
- Each node implements `WorkflowNode.execute(AgentState)` and returns the next node name or `"END"`
- Entry point: `AgentChatController` (POST `/agent/chat`), SSE streaming with `status`/`response`/`error` events
- Provides full management: products CRUD, orders, sessions, sensitive-word filtering

### RAG-based app (LoveApp)

- Uses Spring AI `ChatClient` with advisors pipeline: `MessageChatMemoryAdvisor` (MyBatis-persisted memory) + `QuestionAnswerAdvisor` (PgVector retrieval) + custom advisors
- Documents loaded from `src/main/resources/document/*.md` into PostgreSQL pgvector
- `LoveAppRagCustomAdvisorFactory` configures query rewriting and contextual augmentation

### Tool system

`ToolRegistration` creates all `ToolCallback[]` beans: `FileOperationTool`, `WebSearchTool`, `WebScrapingTool`, `ResourceDownloadTool`, `TerminalOperationTool`, `PDFGenerationTool`, `HtmlGenerationTool`, `ImageSearchTool`, `DateTimeTool`, `TerminateTool`, `EmailSendingTool`

### Data layer

- **MySQL** (MyBatis-Plus): business data — chat memory, coffee products/orders/sessions/sensitive words. Dynamic datasource via `dynamic-datasource-spring-boot-starter`
- **PostgreSQL + pgvector**: vector embeddings for RAG retrieval
- `Result<T>` is the unified API response wrapper (`{code, data, message, traceId}`)

### Frontend (Vue 3 + Arco Design + Tailwind CSS)

- Routes: `/` → IndexApp, `/coffee-shop/agent-chat` → AgentChatView, `/coffee-shop/agent-admin` → AgentAdminView, `/menu/home` → HomeView, `/menu/love-app` → LoveAppView, `/menu/manus-app` → ManusAppView, `/menu/health-app` → HealthAppView
- SSE connections via `EventSource` (GET for simple agents) or fetch + ReadableStream (POST for coffee agent)
- `vue.config.js` proxies `/api` to `localhost:8102`

### Config

- `application-local.yml` — active profile config: server port, DB credentials, DashScope API key, MCP SSE connections, agent coffee-shop settings
- `application-agent.yml` — agent-specific prompts and defaults (coffee-shop intent/product/order/chitchat prompts)
- `CorsConfig` allows all origins with credentials
