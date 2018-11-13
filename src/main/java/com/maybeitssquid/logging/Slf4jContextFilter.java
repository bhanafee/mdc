package com.maybeitssquid.logging;

import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class Slf4jContextFilter extends ContextFilter {

    private Map<String, String> ApplicationIdMap = Collections.emptyMap();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        ApplicationIdMap = Collections.singletonMap(APPLICATION_ID, getApplicationId());
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        MDC.setContextMap(ApplicationIdMap);
        try (final MDC.MDCCloseable ignored = MDC.putCloseable(REQUEST_ID, extractRequestId(request).toString())) {
            chain.doFilter(request, response);
        }
    }
}
