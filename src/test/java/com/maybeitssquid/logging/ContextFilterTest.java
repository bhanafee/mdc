package com.maybeitssquid.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ContextFilterTest {

    private ContextFilter test;

    @BeforeEach
    public void createFilter() {
        this.test = spy(ContextFilter.class);
    }

    @Test
    public void init_applicationIdSet() throws ServletException {
        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter(ContextFilter.APPLICATION_ID)).thenReturn("testApplication");

        Assertions.assertNull(test.getApplicationId());
        test.init(config);
        Assertions.assertEquals("testApplication", test.getApplicationId());
    }

    @Test
    public void destroy_noop() {
        test.destroy();
    }

    @Test
    public void extractRequestId_randomFromNull() {
        final UUID result = test.extractRequestId(null);
        Assertions.assertNotNull(result);
    }

    @Test
    public void extractRequestId_randomFromNonHttpRequest() {
        final ServletRequest request = mock(ServletRequest.class);
        final UUID result = test.extractRequestId(request);
        Assertions.assertNotNull(result);
    }

    @Test
    public void extractRequestId_fromExplicitHeaderValid() {
        final UUID valid = UUID.randomUUID();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ContextFilter.REQUEST_ID_HEADER)).thenReturn(valid.toString());

        final UUID result = test.extractRequestId(request);

        Assertions.assertEquals(valid, result);
    }

    @Test
    public void extractRequestId_fromExplicitHeaderInvalid() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ContextFilter.REQUEST_ID_HEADER)).thenReturn("INVALID");

        final UUID result = test.extractRequestId(request);

        Assertions.assertNotNull(result);
    }

    @Test
    public void extractRequestId_fromTraceparentHeaderInvalid() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("traceparent")).thenReturn("INVALID");

        final UUID result = test.extractRequestId(request);

        Assertions.assertNotNull(result);
    }

    @Test
    public void extractRequestid_fromTraceparentHeaderValid() {
        final UUID expected = new UUID(0x4bf92f3577b34da6L, 0xa3ce929d0e0e4736L);
        final String traceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("traceparent")).thenReturn(traceparent);

        final UUID result = test.extractRequestId(request);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void extractRequestId_fromB3TraceIdHeaderInvalid() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-B3-TraceId")).thenReturn("INVALID");

        final UUID result = test.extractRequestId(request);

        Assertions.assertNotNull(result);
    }

    @Test
    public void extractRequestid_fromB3TradeIdHeaderValid() {
        final UUID expected = new UUID(0x4bf92f3577b34da6L, 0xa3ce929d0e0e4736L);
        final String b3TraceId = "4bf92f3577b34da6a3ce929d0e0e4736";

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-B3-TraceId")).thenReturn(b3TraceId);

        final UUID result = test.extractRequestId(request);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void extractRequestId_fromB3HeaderInvalid() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("b3")).thenReturn("INVALID");

        final UUID result = test.extractRequestId(request);

        Assertions.assertNotNull(result);
    }

    @Test
    public void extractRequestid_fromB3HeaderValidShort() {
        final UUID expected = new UUID(0x4bf92f3577b34da6L, 0xa3ce929d0e0e4736L);
        final String b3 = "4bf92f3577b34da6a3ce929d0e0e4736";

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("b3")).thenReturn(b3);

        final UUID result = test.extractRequestId(request);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void extractRequestid_fromB3HeaderValidComplete() {
        final UUID expected = new UUID(0x80f198ee56343ba8L, 0x64fe8b2a57d3eff7L);
        final String b3 = "80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-1-05e3ac9a4f6e3b90";

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("b3")).thenReturn(b3);

        final UUID result = test.extractRequestId(request);

        Assertions.assertEquals(expected, result);
    }
}
