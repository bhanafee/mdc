package com.maybeitssquid.logging;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

abstract public class WebLogFilter implements Filter {
    /**
     * Formatter for timestamps that follows RFC-3339 with forced millisecond precision and offset time zone style.
     */
    public static final DateTimeFormatter TIMESTAMP = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
            .appendOffset("+HH:MM", "+00:00")
            .toFormatter(Locale.US);

    private ZoneId UTC = ZoneId.of("UTC");

    public static final String WEB_LOG_NAME_PARAMETER = "logName";

    private String logName;

    protected String getLogName() {
        return this.logName == null ? "web" : this.logName;
    }

    abstract void webLog(final Map<String, String> parameters, final boolean failure, final String message) throws IOException;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String logName = filterConfig.getInitParameter(WEB_LOG_NAME_PARAMETER);
        if (logName != null && !logName.isEmpty()) {
            this.logName = logName;
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
            final Map<String, String> parameters = extractWebLogParameters(request, response);
            final boolean failure = response instanceof HttpServletResponse &&
                    ((HttpServletResponse) response).getStatus() >= 400;
            webLog(parameters, failure, null);
        } catch (final IOException | ServletException e) {
            final Map<String, String> parameters = extractWebLogParameters(request, response);
            // TODO: Add exception parameters
            webLog(parameters, true, e.getMessage());
            throw e;
        }
    }

    @Override
    public void destroy() {
        // EMPTY
    }

    public Map<String, String> extractWebLogParameters(final ServletRequest request, final ServletResponse response) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("logEventType", "web");

        extractServletRequest(parameters, request);

        if (request instanceof HttpServletRequest) {
            extractHttpServletRequest(parameters, (HttpServletRequest) request);
            extractHttpRequestHeaders(parameters, (HttpServletRequest) request);

            assert response instanceof HttpServletResponse;
            extractHttpServletResponse(parameters, (HttpServletResponse) response);
        }

        return parameters;
    }

    private void extractServletRequest(final Map<String, String> parameters, final ServletRequest request) {
        parameters.put("src_ip", request.getRemoteAddr());
        parameters.put("src_port", Integer.toString(request.getRemotePort()));
        parameters.put("dest_ip", request.getLocalAddr());
        parameters.put("dest_port", Integer.toString(request.getLocalPort()));
        final int bytes_in = request.getContentLength();
        if (bytes_in > -1) {
            parameters.put("bytes_in", Integer.toString(bytes_in));
        }
        parameters.put("protocol", request.getProtocol());
    }

    private void extractHttpServletResponse(final Map<String, String> parameters, final HttpServletResponse response) {
        parameters.put("http_status", Integer.toString(response.getStatus()));
    }

    private void extractHttpServletRequest(final Map<String, String> parameters, final HttpServletRequest request) {
        parameters.put("http_method", request.getMethod());
        final StringBuffer url = request.getRequestURL();
        if (url != null) {
            parameters.put("url", url.toString());
        }
    }

    private void extractHttpRequestHeaders(final Map<String, String> parameters, final HttpServletRequest request) {
        ifPresentPut(parameters, "http_host", request.getHeader("Host"));
        ifPresentPut(parameters, "http_referrer", request.getHeader("Referer"));
        ifPresentPut(parameters, "http_user_agent", request.getHeader("User-Agent"));
        ifPresentPut(parameters, "http_content_type", request.getHeader("Content-Type"));
        ifPresentPut(parameters, "x_forwarded_for", request.getHeader("X-Forwarded-For"));
        ifPresentPut(parameters, "keep_alive", request.getHeader("Connection"));
        ZonedDateTime date = extractDateHeader(request);
        if (date != null) {
            parameters.put("date", TIMESTAMP.format(date));
        }
    }

    private void ifPresentPut(final Map<String, String> parameters, final String key, final String value) {
        if (value != null && !value.isEmpty()) {
            parameters.put(key, value);
        }
    }

    private ZonedDateTime extractDateHeader(final HttpServletRequest request) {
        ZonedDateTime date = null;

        // Easy way first
        long millis = request.getDateHeader("Date");
        if (millis >= 0L) try {
            date = Instant.ofEpochMilli(millis).atZone(UTC);
        } catch (final IllegalArgumentException e) {
            // EMPTY
        }

        // Try different parser
        if (date == null) {
            String raw = request.getHeader("Date");
            if (raw != null && !raw.isEmpty()) try {
                date = ZonedDateTime.parse(raw, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (final DateTimeException | ArithmeticException e) {
                // EMPTY
            }
        }

        return date;
    }
}
