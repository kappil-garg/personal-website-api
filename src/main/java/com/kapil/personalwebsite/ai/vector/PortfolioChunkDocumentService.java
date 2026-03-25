package com.kapil.personalwebsite.ai.vector;

import com.kapil.personalwebsite.ai.portfolio.PortfolioDataProvider;
import com.kapil.personalwebsite.ai.util.AiTextUtils;
import com.kapil.personalwebsite.ai.util.PortfolioEntityTextBuilder;
import com.kapil.personalwebsite.entity.*;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Builds chunked Documents with RAG metadata for VectorStore indexing.
 * Used by both the RAG context builder and the vector chunk document service to ensure the text stored is identical.
 *
 * @author Kapil Garg
 */
@Service
public class PortfolioChunkDocumentService {

    private static final int CHUNK_MAX_CHARS = 900;
    private static final int CHUNK_OVERLAP = 140;

    private final PortfolioDataProvider dataProvider;

    public PortfolioChunkDocumentService(PortfolioDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Sanitizes an id for use inside Spring AI document ids (avoid ':' from breaking keys).
     *
     * @param id the id to sanitize
     * @return the sanitized id
     */
    private static String safeChunkIdPart(String id) {
        if (id == null || id.isBlank()) {
            return "unknown";
        }
        return id.replace(":", "_");
    }

    /**
     * Builds chunked documents for all portfolio data with consistent RAG metadata for vector embedding and retrieval.
     *
     * @return all chunked documents to embed and store (maybe empty)
     */
    public List<Document> buildAllChunkDocuments() {
        List<Document> documents = new ArrayList<>();
        addPersonalInfoChunks(documents);
        addExperienceChunks(documents);
        addProjectChunks(documents);
        addEducationChunks(documents);
        addCertificationChunks(documents);
        addSkillChunks(documents);
        addBlogChunks(documents);
        return documents;
    }

    /**
     * Adds a chunk series to the list of documents.
     *
     * @param out             the list of documents
     * @param type            the type of the document
     * @param sourceId        the source id of the document
     * @param title           the title of the document
     * @param slug            the slug of the document
     * @param projectIdOrNull the project id or null
     * @param fullText        the full text of the document
     */
    private void addChunkSeries(List<Document> out, String type, String sourceId, String title,
                                String slug, String projectIdOrNull, String fullText) {
        List<String> chunks = TextChunker.chunk(fullText, CHUNK_MAX_CHARS, CHUNK_OVERLAP);
        if (chunks.isEmpty()) {
            return;
        }
        String sid = safeChunkIdPart(sourceId);
        for (int i = 0; i < chunks.size(); i++) {
            String semanticId = type + ":" + sid + ":" + i;
            String docId = UUID.nameUUIDFromBytes(semanticId.getBytes(StandardCharsets.UTF_8)).toString();
            Map<String, Object> meta = new HashMap<>();
            meta.put(PortfolioVectorMetadataKeys.NAMESPACE, PortfolioVectorMetadataKeys.NAMESPACE_PORTFOLIO);
            meta.put(PortfolioVectorMetadataKeys.TYPE, type);
            meta.put(PortfolioVectorMetadataKeys.SOURCE_ID, sourceId != null ? sourceId : sid);
            meta.put(PortfolioVectorMetadataKeys.TITLE, title != null ? title : "");
            meta.put(PortfolioVectorMetadataKeys.CHUNK_INDEX, i);
            if (slug != null && !slug.isBlank()) {
                meta.put(PortfolioVectorMetadataKeys.SLUG, slug);
            }
            if (projectIdOrNull != null && !projectIdOrNull.isBlank()) {
                meta.put(PortfolioVectorMetadataKeys.PROJECT_ID, projectIdOrNull);
            }
            out.add(new Document(docId, chunks.get(i), meta));
        }
    }

    /**
     * Adds the personal info chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addPersonalInfoChunks(List<Document> out) {
        dataProvider.getPersonalInfo().ifPresent(info ->
                addChunkSeries(out, "personal_info", info.getId(), info.getName(), null, null,
                        PortfolioEntityTextBuilder.buildPersonalInfoText(info)));
    }

    /**
     * Adds the experience chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addExperienceChunks(List<Document> out) {
        for (Experience e : dataProvider.getAllExperiences()) {
            addChunkSeries(out, "experience", e.getId(),
                    AiTextUtils.nullSafe(e.getPosition()) + " @ " + AiTextUtils.nullSafe(e.getCompanyName()),
                    null, null,
                    PortfolioEntityTextBuilder.buildExperienceText(e));
        }
    }

    /**
     * Adds the project chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addProjectChunks(List<Document> out) {
        for (Project p : dataProvider.getAllProjects()) {
            String pid = p.getId();
            addChunkSeries(out, "project", pid, p.getTitle(), null, pid,
                    PortfolioEntityTextBuilder.buildProjectText(p));
        }
    }

    /**
     * Adds the education chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addEducationChunks(List<Document> out) {
        for (Education ed : dataProvider.getAllEducations()) {
            addChunkSeries(out, "education", ed.getId(), ed.getInstitutionName(), null, null,
                    PortfolioEntityTextBuilder.buildEducationText(ed));
        }
    }

    /**
     * Adds the certification chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addCertificationChunks(List<Document> out) {
        for (Certification c : dataProvider.getAllCertifications()) {
            addChunkSeries(out, "certification", c.getId(), c.getCertificationName(), null, null,
                    PortfolioEntityTextBuilder.buildCertificationText(c));
        }
    }

    /**
     * Adds the skill chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addSkillChunks(List<Document> out) {
        for (Skill s : dataProvider.getAllSkills()) {
            addChunkSeries(out, "skill", s.getId(), s.getCategoryName(), null, null,
                    PortfolioEntityTextBuilder.buildSkillText(s));
        }
    }

    /**
     * Adds the blog chunks to the list of documents.
     *
     * @param out the list of documents
     */
    private void addBlogChunks(List<Document> out) {
        for (Blog blog : dataProvider.getPublishedBlogs()) {
            addChunkSeries(out, "blog", blog.getSlug(), blog.getTitle(), blog.getSlug(), null,
                    PortfolioEntityTextBuilder.buildBlogText(blog));
        }
    }

}
