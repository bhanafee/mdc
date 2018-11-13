package com.maybeitssquid.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.mockito.Mockito.*;

public class WebLogFilterTest {

    private WebLogFilter test;

    @BeforeEach
    public void createFilter() {
        this.test = new WebLogFilter() {
            @Override
            void webLog(Map<String, String> parameters, boolean failure, String message) throws IOException {
                Assertions.assertEquals("web", parameters.get("logEventType"));
            }
        };
    }

    @Test
    public void TIMESTAMP_formatting() {
        final ZonedDateTime EPOCH =
                ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        Assertions.assertEquals("1970-01-01T00:00:00.000+00:00", WebLogFilter.TIMESTAMP.format(EPOCH));
    }

    @Test
    public void getLogName_default() {
        Assertions.assertEquals("web", test.getLogName());
    }

    @Test
    public void init_setsLogName() throws ServletException {
        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter(WebLogFilter.WEB_LOG_NAME_PARAMETER)).thenReturn("testLog");

        test.init(config);
        Assertions.assertEquals("testLog", test.getLogName());
    }

    @Test
    public void doFilter_normalFlow() throws IOException, ServletException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        // Log as a failure to increase code coverage
        when(response.getStatus()).thenReturn(500);

        test.doFilter(request, response, chain);
    }

    @Test
    public void doFilter_exceptionFlow() throws IOException, ServletException{
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        final ServletException CHAINED_FAILURE = new ServletException();
        doThrow(CHAINED_FAILURE).when(chain).doFilter(request, response);

        try {
            test.doFilter(request, response, chain);
        } catch (final IOException | ServletException e) {
            Assertions.assertSame(CHAINED_FAILURE, e);
        }

    }

    @Test
    public void destroy_noop() {
        test.destroy();
    }

    @Test
    public void extractWebLogParameters_logEventType() {
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("web", result.get("logEventType"));
    }

    @Test
    public void extractWebLogParameters_servletRequest() {
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);

        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getRemotePort()).thenReturn(8080);
        when(request.getLocalAddr()).thenReturn("127.0.0.1");
        when(request.getLocalPort()).thenReturn(80);
        when(request.getContentLength()).thenReturn(1);
        when(request.getProtocol()).thenReturn("http");

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("10.0.0.1", result.get("src_ip"));
        Assertions.assertEquals("8080", result.get("src_port"));
        Assertions.assertEquals("127.0.0.1", result.get("dest_ip"));
        Assertions.assertEquals("80", result.get("dest_port"));
        Assertions.assertEquals("1", result.get("bytes_in"));
        Assertions.assertEquals("http", result.get("protocol"));
    }

    @Test
    public void extractWebLogParameters_httpRequest() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/"));

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("GET", result.get("http_method"));
        Assertions.assertEquals("http://localhost/", result.get("url"));
    }

    @Test
    public void extractWebLogParameters_httpResponse() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getStatus()).thenReturn(200);

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("200", result.get("http_status"));
    }

    @Test
    public void extractWebLogParameters_httpHeaders() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getHeader("Host")).thenReturn("example.com");
        when(request.getHeader("Referer")).thenReturn("http://example.com");
        when(request.getHeader("User-Agent")).thenReturn("curl/7.54.0");
        when(request.getHeader("Content-Type")).thenReturn("text/html");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.0.1");
        when(request.getHeader("Connection")).thenReturn("keep-alive");
        when(request.getDateHeader("Date")).thenReturn(-1L);

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("example.com", result.get("http_host"));
        Assertions.assertEquals("http://example.com", result.get("http_referrer"));
        Assertions.assertEquals("curl/7.54.0", result.get("http_user_agent"));
        Assertions.assertEquals("text/html", result.get("http_content_type"));
        Assertions.assertEquals("192.168.0.1", result.get("x_forwarded_for"));
        Assertions.assertEquals("keep-alive", result.get("keep_alive"));
        Assertions.assertNull(result.get("date"));
    }

    @Test
    public void extractWebLogParameters_httpDateHeaderStandardParse() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getDateHeader("Date")).thenReturn(0L);

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("1970-01-01T00:00:00.000+00:00", result.get("date"));
    }

    @Test
    public void extractWebLogParameters_httpDateHeaderFallbackParse() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // Force fallback from built-in date parser
        when(request.getDateHeader("Date")).thenReturn(-1L);
        when(request.getHeader("Date")).thenReturn("2011-12-03T10:15:30+01:00");

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertEquals("2011-12-03T10:15:30.000+01:00", result.get("date"));
    }

    @Test
    public void extractWebLogParameters_httpDateHeaderFailedParse() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // Force fallback from built-in date parser
        when(request.getDateHeader("Date")).thenReturn(-1L);
        when(request.getHeader("Date")).thenReturn("INVALID");

        final Map<String, String> result = test.extractWebLogParameters(request, response);

        Assertions.assertNull(result.get("date"));
    }
}
