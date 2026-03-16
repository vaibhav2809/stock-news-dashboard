package com.stocknews.controller;

import com.stocknews.dto.*;
import com.stocknews.security.AuthenticatedUser;
import com.stocknews.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Spaces (reading lists) and their saved articles.
 * All endpoints require JWT authentication. The user ID is extracted
 * from the SecurityContext via {@link AuthenticatedUser}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name = "Spaces", description = "Reading list management endpoints")
public class SpaceController {

    private final SpaceService spaceService;

    /**
     * Creates a new space (reading list).
     *
     * @param request the space details (name, description)
     * @return the created space
     */
    @PostMapping
    @Operation(summary = "Create a space", description = "Create a new reading list for saving articles")
    @ApiResponse(responseCode = "201", description = "Space created successfully")
    @ApiResponse(responseCode = "409", description = "A space with this name already exists")
    public ResponseEntity<SpaceResponse> createSpace(@Valid @RequestBody SpaceRequest request) {
        log.info("POST /api/v1/spaces — name={}", request.getName());
        final SpaceResponse response = spaceService.createSpace(AuthenticatedUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all spaces for the current user.
     *
     * @return list of spaces
     */
    @GetMapping
    @Operation(summary = "List spaces", description = "List all reading lists for the current user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved spaces")
    public ResponseEntity<List<SpaceResponse>> listSpaces() {
        log.info("GET /api/v1/spaces");
        final List<SpaceResponse> spaces = spaceService.getSpacesByUserId(AuthenticatedUser.getUserId());
        return ResponseEntity.ok(spaces);
    }

    /**
     * Gets a specific space by ID.
     *
     * @param id the space ID
     * @return the space details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get space by ID", description = "Retrieve a specific reading list by its ID")
    @ApiResponse(responseCode = "200", description = "Space found")
    @ApiResponse(responseCode = "404", description = "Space not found")
    public ResponseEntity<SpaceResponse> getSpaceById(
            @Parameter(description = "Space ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/spaces/{}", id);
        final SpaceResponse response = spaceService.getSpaceById(id, AuthenticatedUser.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a space's name and/or description.
     *
     * @param id      the space ID
     * @param request the updated details
     * @return the updated space
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a space", description = "Update a reading list's name or description")
    @ApiResponse(responseCode = "200", description = "Space updated successfully")
    @ApiResponse(responseCode = "404", description = "Space not found")
    @ApiResponse(responseCode = "409", description = "A space with this name already exists")
    public ResponseEntity<SpaceResponse> updateSpace(
            @Parameter(description = "Space ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SpaceRequest request
    ) {
        log.info("PUT /api/v1/spaces/{} — name={}", id, request.getName());
        final SpaceResponse response = spaceService.updateSpace(id, AuthenticatedUser.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a space and all its saved articles.
     *
     * @param id the space ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a space", description = "Delete a reading list and all its saved articles")
    @ApiResponse(responseCode = "204", description = "Space deleted successfully")
    @ApiResponse(responseCode = "404", description = "Space not found")
    public ResponseEntity<Void> deleteSpace(
            @Parameter(description = "Space ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/spaces/{}", id);
        spaceService.deleteSpace(id, AuthenticatedUser.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Saves an article to a space.
     *
     * @param id      the space ID
     * @param request the article data to save
     * @return the saved article
     */
    @PostMapping("/{id}/articles")
    @Operation(summary = "Save article to space", description = "Save a news article to a reading list")
    @ApiResponse(responseCode = "201", description = "Article saved successfully")
    @ApiResponse(responseCode = "404", description = "Space not found")
    @ApiResponse(responseCode = "409", description = "Article already saved in this space")
    public ResponseEntity<SavedArticleResponse> saveArticle(
            @Parameter(description = "Space ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SaveArticleRequest request
    ) {
        log.info("POST /api/v1/spaces/{}/articles — title={}", id, request.getTitle());
        final SavedArticleResponse response = spaceService.saveArticleToSpace(id, AuthenticatedUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all saved articles in a space.
     *
     * @param id the space ID
     * @return list of saved articles
     */
    @GetMapping("/{id}/articles")
    @Operation(summary = "List articles in space", description = "Get all saved articles in a reading list")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved articles")
    @ApiResponse(responseCode = "404", description = "Space not found")
    public ResponseEntity<List<SavedArticleResponse>> listArticlesInSpace(
            @Parameter(description = "Space ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/spaces/{}/articles", id);
        final List<SavedArticleResponse> articles = spaceService.getArticlesInSpace(id, AuthenticatedUser.getUserId());
        return ResponseEntity.ok(articles);
    }

    /**
     * Removes a saved article from a space.
     *
     * @param id        the space ID
     * @param articleId the saved article ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}/articles/{articleId}")
    @Operation(summary = "Remove article from space", description = "Remove a saved article from a reading list")
    @ApiResponse(responseCode = "204", description = "Article removed successfully")
    @ApiResponse(responseCode = "404", description = "Space or article not found")
    public ResponseEntity<Void> removeArticle(
            @Parameter(description = "Space ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Saved article ID", example = "1")
            @PathVariable Long articleId
    ) {
        log.info("DELETE /api/v1/spaces/{}/articles/{}", id, articleId);
        spaceService.removeArticleFromSpace(id, articleId, AuthenticatedUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
