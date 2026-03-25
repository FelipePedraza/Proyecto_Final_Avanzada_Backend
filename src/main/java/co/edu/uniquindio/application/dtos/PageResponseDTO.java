package co.edu.uniquindio.application.dtos;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 * Includes both the content and pagination metadata.
 *
 * @param <T> the type of elements in the content list
 */
public record PageResponseDTO<T>(
        List<T> content,
        PaginationMetadata pagination
) {

    /**
     * Creates a PageResponseDTO from a Spring Data Page object.
     *
     * @param page the Spring Data Page
     * @param <T>  the type of elements
     * @return a new PageResponseDTO with pagination metadata
     */
    public static <T> PageResponseDTO<T> fromPage(Page<T> page) {
        PaginationMetadata metadata = new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
        return new PageResponseDTO<>(page.getContent(), metadata);
    }

    /**
     * Pagination metadata for the response.
     */
    public record PaginationMetadata(
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean isFirst,
            boolean isLast,
            boolean hasNext,
            boolean hasPrevious
    ) {
    }
}
