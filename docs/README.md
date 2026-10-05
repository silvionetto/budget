# Documentation index

This folder is organized for fast retrieval by both humans and LLMs working on the project.

## Recommended starting points

- Overview: [overview/project-overview.md](overview/project-overview.md)
- Domain model: [domain/domain-model.md](domain/domain-model.md)
- Architecture: [architecture/system-architecture.md](architecture/system-architecture.md)
- Budget logic: [implementation/budget-calculation.md](implementation/budget-calculation.md)
- Routes and controllers: [implementation/routes-and-controllers.md](implementation/routes-and-controllers.md)
- Source map: [references/source-map.md](references/source-map.md)
- Legacy reverse-engineering write-up: [reverse-engineering.md](reverse-engineering.md)

## Document map

### Overview
- `overview/project-overview.md` — purpose, stack, and project scope.

### Domain
- `domain/domain-model.md` — entities, relationships, and persistence semantics.

### Architecture
- `architecture/system-architecture.md` — runtime flow, package responsibilities, and bootstrap behavior.

### Implementation
- `implementation/budget-calculation.md` — how income, expense, totals, and projected balances are computed.
- `implementation/routes-and-controllers.md` — all page routes and their data responsibilities.

### References
- `references/source-map.md` — file-to-feature mapping for the main Kotlin source files and templates.

## Retrieval prompt patterns

When looking for a specific answer, use the document closest to the question:

- “What is this app?” → `overview/project-overview.md`
- “What is the data model?” → `domain/domain-model.md`
- “What does the app boot with?” → `architecture/system-architecture.md`
- “How are annual budgets computed?” → `implementation/budget-calculation.md`
- “Which route renders which page?” → `implementation/routes-and-controllers.md`
- “Which file owns which behavior?” → `references/source-map.md`
