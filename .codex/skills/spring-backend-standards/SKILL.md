---
name: spring-backend-standards
description: Apply shared implementation standards for Java Spring backends. Use when Codex needs to create or update modules while following naming conventions, request and response mapping, constructor-based injection, Swagger documentation, repository rules, JPA foreign-key naming, status enums with transitions, filtered search, Excel exports, security, and standardized error handling.
---

# Spring Backend Standards

Use this skill as the top-level guide for backend work. Then load the narrower skill that matches the task.

## Naming and contract rules

- Use `EntityRequest` for input classes.
- Use `EntityResponse` for complete output models.
- Use `EntityBasicResponse` for compact or nested output models with basic information only.
- Do not use `form` or `dto` naming in new code unless the target codebase already forces that contract.
- Keep variable names, method names, class fields, and private helpers in English.
- Use Portuguese only in Swagger/OpenAPI descriptions meant to be shown to API consumers.
- Avoid `var`. Use explicit types unless inference is genuinely necessary to keep the code readable.
- Do not use Lombok under any circumstance. Do not generate `@Getter`, `@Setter`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, or any other Lombok annotation.
- Avoid excessive helper methods. Extract helpers only when they remove real duplication or isolate non-trivial logic cleanly.
- Document controllers, requests, responses, enums, and relevant models with Swagger/OpenAPI.

## Mapping rules

- Prefer `Entity(Request request)` constructors whenever possible.
- Prefer `EntityResponse(Entity entity)` and `EntityBasicResponse(Entity entity)` constructors whenever possible.
- Keep repetitive field mapping out of services when constructor mapping is sufficient.

## Dependency injection rules

- Use constructor injection in services and controllers.
- Keep repositories as plain Spring Data interfaces without `@Repository` unless the target codebase requires an exception.

## Date parameter rules

- Use `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` for `LocalDate`.
- Use `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` for `LocalDateTime`, `OffsetDateTime`, or other date-time request parameters.

## Persistence naming rules

- Use `snake_case` for table names and non-foreign-key column names in `@Table(name = ...)` and `@Column(name = ...)`.
- For foreign keys, use `fk_Id_NomeDaColuna` in `@JoinColumn(name = ...)`.
- For foreign key constraint names, use `FK_FROM_TBTABELAORIGEM_FOR_TBTABELADESTINO`.
- Do not replace the existing foreign-key naming pattern with `snake_case`.

## Entity audit rules

- When the entity supports both creation and update timestamps, add `created_at` and `updated_at` fields with `LocalDateTime`, plus `@PrePersist` and `@PreUpdate`.
- When the entity is immutable after creation or has no update flow, add only `created_at` with `LocalDateTime` plus `@PrePersist`.
- Keep audit field names in Java as `createdAt` and `updatedAt`.

```java
@Column(name = "created_at", updatable = false, nullable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

@PrePersist
private void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
}

@PreUpdate
private void preUpdate() {
    this.updatedAt = LocalDateTime.now();
}
```

```java
@Column(name = "created_at", updatable = false, nullable = false)
private LocalDateTime createdAt;

@PrePersist
private void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
}
```

## API and springdoc rules

- When touching Swagger/OpenAPI configuration, inspect the existing springdoc config first instead of assuming it is correct.
- Verify that the configured title, description, and grouped API metadata match the current application. Fix copied text from other projects when it is incoherent.
- When `springdoc.swagger-ui.tagsSorter=alpha` is missing, add it.
- When `springdoc.swagger-ui.operationsSorter=alpha` is missing, add it.
- When OpenAPI metadata has no version, add one starting at `1`.
- Preserve existing config style and file placement when updating `application.yml`, `application.properties`, or Java-based OpenAPI config.

## Controller and service pattern

- Reuse the project's existing controller and service structure when it already matches this pattern.
- Add the pattern below only when the target module does not already provide an equivalent structure.
- When loading by id for normal entity retrieval, call the service `findById(...)` instead of accessing the repository directly from controllers or from another service layer shortcut.
- Before adding a repository lookup by id, first check whether the target entity already has a service with `findById(...)` or an equivalent ready method and reuse it.
- Use direct repository access for id lookups only when the task explicitly needs a different projection, lock mode, existence check, or query shape.

```java
@RestController
@RequestMapping("/resource-name")
@Validated
public class XxxController {

    private final XxxService xxxService;

    public XxxController(XxxService xxxService) {
        this.xxxService = xxxService;
    }

    @Operation(summary = "...")
    @ApiResponse(responseCode = "200", description = "...")
    @GetMapping("/{id}")
    public ResponseEntity<XxxResponse> findById(@PathVariable Long id) {
        Xxx xxx = xxxService.findById(id);
        return ResponseEntity.ok(new XxxResponse(xxx));
    }
}
```

```java
@Service
public class XxxService {

    private final XxxRepository xxxRepository;

    public XxxService(XxxRepository xxxRepository) {
        this.xxxRepository = xxxRepository;
    }

    @Transactional(readOnly = true)
    public Xxx findById(Long id) {
        return xxxRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Xxx não encontrada para ID: " + id));
    }

    @Transactional
    public Xxx create(XxxRequest request) {
        Xxx xxx = new Xxx(request);
        return xxxRepository.save(xxx);
    }
}
```

- Response codes to prefer: `200 OK`, `201 CREATED`, `204 NO_CONTENT`, `404 NOT_FOUND`.
- For responses with body, prefer `ResponseEntity.ok(body)` or `ResponseEntity.status(201).body(body)`.
- For responses without body, prefer `ResponseEntity.noContent().build()`.
- Use `EntityNotFoundException` for not-found cases.
- Use `DataIntegrityViolationException` for business rule violations.

## Skill routing

- For CRUD module creation or refactor: use `spring-crud-module`.
- For dynamic filtered search: use `spring-filters-specification`.
- For Excel exports: use `spring-report-excel`.
- For JWT auth and stateless security: use `spring-security-jwt`.
- For global error handling and request logging: use `spring-error-logging`.
- For status enums with transition rules: use `spring-status-enums`.
- For JUnit and Mockito tests: use `spring-tests-junit-mockito`.
- For `@Lob String` storage of Markdown or long text content: use `spring-md-content-storage`.
- For binary document/image/video entity storage with database-specific mapping: use `spring-document-storage`.
