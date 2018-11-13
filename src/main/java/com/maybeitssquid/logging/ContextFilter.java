package com.maybeitssquid.logging;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class ContextFilter implements Filter {

    /**
     * Key to the application ID
     */
    public static final String APPLICATION_ID = "applicationId";

    public static final String REQUEST_ID = "WFRequestId";

    public static final String REQUEST_ID_HEADER = REQUEST_ID;

    public static final String TRACE_PARENT_HEADER = "traceparent";

    public static final String B3_TRACE_ID_MULTI_HEADER = "X-B3-TraceId";

    public static final String B3_SINGLE_HEADER = "b3";

    /**
     * Parse HTTP {@link #TRACE_PARENT_HEADER} header
     */
    private static final Pattern TRACE_PARENT_PATTERN = Pattern.compile(
            "\\p{XDigit}{2}-(?<traceId>\\p{XDigit}{32})-\\p{XDigit}{16}-\\p{XDigit}{2}");

    /**
     * Parse HTTP {@link #B3_TRACE_ID_MULTI_HEADER} header. Only accepts 128 bit trace ids.
     */
    private static final Pattern B3_TRACE_ID_MULTI_PATTERN = Pattern.compile("(?<traceId>\\p{XDigit}{32})");

    /**
     * Parse HTTP {@link #B3_SINGLE_HEADER} header. Only accepts 128 bit trace ids.
     */
    private static final Pattern B3_TRACE_PATTERN = Pattern.compile(
            "(?<traceId>\\p{XDigit}{32})(-\\p{XDigit}{16})?(-\\p{XDigit})?(-\\p{XDigit}{16})?");

    private String applicationId;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.applicationId = filterConfig.getInitParameter(APPLICATION_ID);
    }

    @Override
    public void destroy() {
        // EMPTY
    }

    public String getApplicationId() {
        return applicationId;
    }

    public UUID extractRequestId(final ServletRequest request) {
        UUID requestId = null;

        if (request instanceof HttpServletRequest) {
            requestId = extractRequestIdFromExplicitHeader((HttpServletRequest) request);

            if (requestId == null) {
                requestId = extractRequestIdFromTraceId((HttpServletRequest) request);
            }
        }

        return requestId == null ? UUID.randomUUID() : requestId;
    }

    private UUID extractRequestIdFromExplicitHeader(final HttpServletRequest request) {
        final String explicit = request.getHeader(REQUEST_ID_HEADER);
        try {
            return explicit == null ? null : UUID.fromString(explicit);
        } catch (final IllegalArgumentException e) {
            // Failed to parse explicit
            return null;
        }
    }

    private UUID extractRequestIdFromTraceId(final HttpServletRequest request) {
        String traceId = null;

        final String traceParent = request.getHeader(TRACE_PARENT_HEADER);
        if (traceParent != null) {
            final Matcher parent = TRACE_PARENT_PATTERN.matcher(traceParent);
            if (parent.matches()) {
                traceId = parent.group("traceId");
            }
        }

        if (traceId == null) {
            final String b3TraceId = request.getHeader(B3_TRACE_ID_MULTI_HEADER);
            if (b3TraceId != null) {
                final Matcher b3Multi = B3_TRACE_ID_MULTI_PATTERN.matcher(b3TraceId);
                if (b3Multi.matches()) {
                    traceId = b3Multi.group("traceId");
                }
            }
        }

        if (traceId == null) {
            final String b3 = request.getHeader(B3_SINGLE_HEADER);
            if (b3 != null) {
                final Matcher b3Single = B3_TRACE_PATTERN.matcher(b3);
                if (b3Single.matches()) {
                    traceId = b3Single.group("traceId");
                }
            }
        }

        assert traceId == null || Pattern.matches("\\p{XDigit}{32}", traceId);

        if (traceId == null) {
            return null;
        } else {
            final long msb = Long.parseUnsignedLong(traceId.substring(0, 16), 16);
            final long lsb = Long.parseUnsignedLong(traceId.substring(16), 16);
            return new UUID(msb, lsb);
        }
    }

}
