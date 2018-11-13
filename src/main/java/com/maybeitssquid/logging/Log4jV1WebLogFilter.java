package com.maybeitssquid.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.Map;

public class Log4jV1WebLogFilter extends WebLogFilter {

    private Logger logger;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.logger = Logger.getLogger(getLogName());
    }

    @Override
    void webLog(final Map<String, String> parameters, final boolean failure, final String message) throws IOException {
        final String m = message == null || message.isEmpty() ? "" : message;

        parameters.forEach((k, v) -> MDC.put(k, v));

        if (failure) {
            logger.warn(m);
        } else {
            logger.info(m);
        }

        parameters.forEach((k, v) -> MDC.remove(k));
    }

}
