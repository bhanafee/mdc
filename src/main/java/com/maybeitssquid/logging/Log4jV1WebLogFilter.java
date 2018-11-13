package com.maybeitssquid.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.io.IOException;
import java.util.Map;

public class Log4jV1WebLogFilter extends WebLogFilter<Logger> {

    @Override
    public void setLogger(String name) {
        setLogger(Logger.getLogger(name));
    }

    @Override
    public void webLog(final Map<String, String> parameters, final boolean failure, final String message) throws IOException {
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
