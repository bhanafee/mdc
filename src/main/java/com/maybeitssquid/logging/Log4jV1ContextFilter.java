package com.maybeitssquid.logging;

import org.apache.log4j.MDC;
import org.apache.log4j.NDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class Log4jV1ContextFilter extends ContextFilter {
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        NDC.clear();
        MDC.clear();
        MDC.put(APPLICATION_ID, getApplicationId());
        MDC.put(REQUEST_ID, extractRequestId(request));
        chain.doFilter(request, response);
        MDC.remove(REQUEST_ID);
    }
}
