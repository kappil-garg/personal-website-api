package com.kapil.personalwebsite.repository;

import com.kapil.personalwebsite.entity.Blog;
import com.kapil.personalwebsite.entity.BlogStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Blog entity operations in the database.
 *
 * @author Kapil Garg
 */
@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {

    /**
     * Finds all active blogs ordered by creation date in descending order.
     *
     * @return list of active blogs
     */
    List<Blog> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Finds a blog by its slug if it is active.
     *
     * @param slug the slug of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    Optional<Blog> findBySlugAndIsActiveTrue(String slug);

    /**
     * Finds a blog by its ID if it is active.
     *
     * @param id the ID of the blog
     * @return an Optional containing the blog if found, or empty if not found
     */
    Optional<Blog> findByIdAndIsActiveTrue(String id);

    /**
     * Checks if a blog exists by its slug.
     *
     * @param slug the slug of the blog
     * @return true if a blog with the given slug exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Finds all published blogs ordered by published date in descending order.
     *
     * @param status the blog status
     * @return list of published blogs
     */
    List<Blog> findByStatusAndIsActiveTrueOrderByPublishedAtDesc(BlogStatus status);

    /**
     * Finds a published blog by its slug.
     *
     * @param slug   the slug of the blog
     * @param status the blog status
     * @return an Optional containing the published blog if found, or empty if not found
     */
    Optional<Blog> findBySlugAndStatusAndIsActiveTrue(String slug, BlogStatus status);

    /**
     * Finds a published blog by its ID.
     *
     * @param id     the ID of the blog
     * @param status the blog status
     * @return an Optional containing the published blog if found, or empty if not found
     */
    Optional<Blog> findByIdAndStatusAndIsActiveTrue(String id, BlogStatus status);

}
