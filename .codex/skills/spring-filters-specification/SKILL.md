---
name: spring-filters-specification
description: Build or update dynamic filtering with Spring Data JPA Specification. Use when Codex needs optional query parameters, date ranges, status filters, text search across fields, joins, pageable listing endpoints, or a single `Specification` class whose methods internally ignore null or empty inputs and are combined directly inside the service layer.
---

# Spring Specification Pattern

Implement filtered search with a single `Specification` class and service-side composition.

## Read first

- One or more `Specification` classes already present in the codebase
- The listing controller for the target entity
- The target repository
- `references/filter-patterns.md`

## Structure

Create one `Specification` class per aggregate or resource being filtered.

## Composition rules

- Build the combined specification directly inside the service layer.
- Start from `Specification.where(...)` or `Specification.allOf(...)`, depending on the codebase preference.
- Chain `.and(...)` and `.or(...)` in the service with the static methods exposed by the `Specification` class.
- Prefer the direct repository call pattern: `repository.findAll(SpecA(...).and(SpecB(...)).and(SpecC(...)), pageable)`.
- Keep one service method for `Page<T>` and another for `List<T>` when reports/export need the same filter without pagination.
- Delegate execution to the repository.

## Specification class rules

- Use static methods returning `Specification<Entity>`.
- Keep each predicate isolated in a small method.
- Each method must internally check whether the incoming parameter is null, blank, or empty.
- When a filter should not be applied, return `Specification.unrestricted()`.
- Use joins only where required.
- Use `builder.between` for date ranges.
- Use `builder.like(builder.lower(...), "%term%")` for case-insensitive string matching.
- Keep `searchAllFields` limited to basic string attributes unless the task explicitly needs relationship traversal.
- For generic search across associated entities or `@ElementCollection`, prefer metamodel-driven traversal only when the codebase already accepts that level of complexity.

## Controller rules

- Expose filters as optional `@RequestParam`.
- Use `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` for `LocalDate`.
- Use `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` for `LocalDateTime`, `OffsetDateTime`, or equivalent date-time parameters.
- Accept lists for multi-status and multi-sector filters.
- Pass `Pageable` straight through to the service.
- Document filter parameters with Swagger/OpenAPI when the project documents listing endpoints.

## Constraints

- Keep parameter names aligned across controller, service, and specification.
- Do not create a separate `Filters` service just to assemble specifications.
- Do not embed filter logic directly in controllers or repositories.
- Prefer English names for parameters, variables, and methods. Keep Portuguese only for Swagger/OpenAPI descriptions meant for users.
