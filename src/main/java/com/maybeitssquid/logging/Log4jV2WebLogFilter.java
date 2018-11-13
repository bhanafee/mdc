package com.maybeitssquid.logging;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class Log4jV2WebLogFilter extends WebLogFilter<Logger> {

    @Override
    public void setLogger(String name) {
        setLogger(LogManager.getLogger(name));
    }

    @Override
    public void webLog(final Map<String, String> parameters, final boolean failure, final String message) throws IOException {
        final String m = message == null || message.isEmpty() ? "" : message;

        try (final CloseableThreadContext.Instance ignore = CloseableThreadContext.putAll(parameters)) {
            if (failure) {
                logger.warn(m);
            } else {
                logger.info(m);
            }
        }
    }

}
