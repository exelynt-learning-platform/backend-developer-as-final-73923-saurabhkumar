package com.booking.controller;

import com.booking.dto.request.ResourceRequest;
import com.booking.dto.response.ResourceResponse;
import com.booking.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Resources", description = "Resource management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    @Operation(summary = "Get all resources", description = "Get paginated list of all resources (USER & ADMIN)")
    public ResponseEntity<Page<ResourceResponse>> getAllResources(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(resourceService.getAllResources(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID", description = "Get a single resource by its ID (USER & ADMIN)")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PostMapping
    @Operation(summary = "Create resource", description = "Create a new resource (ADMIN only)")
    public ResponseEntity<ResourceResponse> createResource(
            @Valid @RequestBody ResourceRequest request) {
        ResourceResponse response = resourceService.createResource(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update resource", description = "Update an existing resource (ADMIN only)")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete resource", description = "Delete a resource (ADMIN only)")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
