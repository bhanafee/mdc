package com.maybeitssquid.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.mockito.Mockito.*;

abstract public class AbstractContextFilterTest {

    protected ContextFilter test;

    protected void initContextFilter() throws ServletException {
        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter("applicationId")).thenReturn("testApplication");
        // Init extension changes internal state that can't be tested externally
        test.init(config);
    }

    abstract String fromContext(final String key);

    @Test
    public void doFilter_setApplicationId() throws IOException, ServletException {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);


        // applicationId should be set in MDC during chain
        FilterChain chain = (ServletRequest req, ServletResponse res) ->
                Assertions.assertEquals("testApplication", fromContext(ContextFilter.APPLICATION_ID));

        test.doFilter(request, response, chain);

        // applicationId should remain in MDC after chain
        Assertions.assertEquals("testApplication", fromContext(ContextFilter.APPLICATION_ID));
    }

    @Test
    public void doFilter_setMDCFromRandom() throws IOException, ServletException {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);

        // requestId should be set in MDC during chain
        FilterChain chain = (ServletRequest req, ServletResponse res) ->
                Assertions.assertNotNull(fromContext(ContextFilter.REQUEST_ID));

        test.doFilter(request, response, chain);

        // requestId should be removed from MDC after chain
        Assertions.assertNull(fromContext(ContextFilter.REQUEST_ID));
    }

    @Test
    public void doFilter_setMDCFromHeader() throws IOException, ServletException {
        final String TEST_REQUEST_ID = "4f74fb4d-ad78-42ab-914a-263a53266ac5";

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ContextFilter.REQUEST_ID_HEADER)).thenReturn(TEST_REQUEST_ID);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // requestId should be set in MDC during chain
        FilterChain chain = (ServletRequest req, ServletResponse res) ->
                Assertions.assertEquals(TEST_REQUEST_ID, fromContext(ContextFilter.REQUEST_ID));

        test.doFilter(request, response, chain);

        // requestId should be removed from MDC after chain
        Assertions.assertNull(fromContext(ContextFilter.REQUEST_ID));
    }
}
