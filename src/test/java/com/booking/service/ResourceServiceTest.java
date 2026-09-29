package com.booking.service;

import com.booking.dto.request.ResourceRequest;
import com.booking.dto.response.ResourceResponse;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Resource;
import com.booking.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource sampleResource;

    @BeforeEach
    void setUp() {
        sampleResource = Resource.builder()
                .id(1L)
                .name("Conference Room A")
                .description("A large room")
                .type("ROOM")
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return paginated resources")
    void shouldGetAllResources() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Resource> page = new PageImpl<>(List.of(sampleResource));
        when(resourceRepository.findAll(pageable)).thenReturn(page);

        Page<ResourceResponse> result = resourceService.getAllResources(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Conference Room A");
    }

    @Test
    @DisplayName("Should return resource by ID")
    void shouldGetResourceById() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));

        ResourceResponse result = resourceService.getResourceById(1L);

        assertThat(result.getName()).isEqualTo("Conference Room A");
        assertThat(result.getType()).isEqualTo("ROOM");
    }

    @Test
    @DisplayName("Should throw when resource not found")
    void shouldThrowWhenResourceNotFound() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.getResourceById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create resource")
    void shouldCreateResource() {
        ResourceRequest request = new ResourceRequest("New Room", "Description", "ROOM", true);
        when(resourceRepository.save(any(Resource.class))).thenReturn(sampleResource);

        ResourceResponse result = resourceService.createResource(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Conference Room A");
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should update resource")
    void shouldUpdateResource() {
        ResourceRequest request = new ResourceRequest("Updated Room", "Updated desc", "ROOM", false);
        Resource updatedResource = Resource.builder()
                .id(1L).name("Updated Room").description("Updated desc")
                .type("ROOM").available(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);

        ResourceResponse result = resourceService.updateResource(1L, request);

        assertThat(result.getName()).isEqualTo("Updated Room");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should delete resource")
    void shouldDeleteResource() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource));

        resourceService.deleteResource(1L);

        verify(resourceRepository).delete(sampleResource);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent resource")
    void shouldThrowWhenDeletingNonExistentResource() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.deleteResource(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
