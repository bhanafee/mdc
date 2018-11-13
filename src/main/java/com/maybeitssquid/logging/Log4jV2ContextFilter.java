package com.maybeitssquid.logging;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.CloseableThreadContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class Log4jV2ContextFilter extends ContextFilter {
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        ThreadContext.clearAll();
        ThreadContext.put(APPLICATION_ID, getApplicationId());
        try (final CloseableThreadContext.Instance ignored = CloseableThreadContext
                .put(REQUEST_ID, extractRequestId(request).toString())) {
            chain.doFilter(request, response);
        }
    }
}
