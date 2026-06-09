# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for the XToPDF project.

## What is an ADR?

An ADR is a short document capturing an important architectural decision along with its context and consequences. We use ADRs to:

- Record decisions that have significant impact on the system
- Provide context for future developers about why things are the way they are
- Track the evolution of the architecture over time

## Index

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-0000](ADR-0000-template.md) | Template | — |
| [ADR-0001](ADR-0001-hexagonal-architecture.md) | Use hexagonal architecture for container orchestration | Accepted |
| [ADR-0002](ADR-0002-abstract-file-converter-pattern.md) | Use AbstractFileConverter template method pattern | Accepted |
| [ADR-0003](ADR-0003-token-bucket-rate-limiting.md) | Use token bucket algorithm for rate limiting | Accepted |
| [ADR-0004](ADR-0004-caffeine-caching.md) | Use Caffeine for conversion result caching | Accepted |
| [ADR-0005](ADR-0005-virtual-threads.md) | Use virtual threads for async/batch processing | Accepted |

## Creating a New ADR

1. Copy `ADR-0000-template.md` to `ADR-NNNN-short-title.md`
2. Fill in all sections
3. Set status to "Proposed"
4. Submit for review
5. Update status to "Accepted" once consensus is reached

## Statuses

- **Proposed** — Under discussion, not yet agreed upon
- **Accepted** — Agreed and implemented
- **Deprecated** — No longer relevant (superseded or removed)
- **Superseded** — Replaced by a newer ADR (link to replacement)
