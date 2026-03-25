package com.kapil.personalwebsite.ai.vector;

import com.kapil.personalwebsite.util.AppConstants;

/**
 * Metadata keys stored on every chunked document in the portfolio vector index.
 *
 * @author Kapil Garg
 */
public final class PortfolioVectorMetadataKeys {

    public static final String NAMESPACE = "namespace";
    public static final String NAMESPACE_PORTFOLIO = "portfolio_rag";

    public static final String TYPE = "type";
    public static final String SOURCE_ID = "sourceId";
    public static final String PROJECT_ID = "projectId";
    public static final String TITLE = "title";
    public static final String SLUG = "slug";
    public static final String CHUNK_INDEX = "chunkIndex";

    private PortfolioVectorMetadataKeys() {
        throw new UnsupportedOperationException(AppConstants.UTILITY_CLASS_INSTANTIATION_MSG);
    }

}
