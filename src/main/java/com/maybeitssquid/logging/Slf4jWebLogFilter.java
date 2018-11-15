package com.maybeitssquid.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Slf4jWebLogFilter extends WebLogFilter<Logger> {

    @Override
    public void setLogger(String name) {
        setLogger(LoggerFactory.getLogger(name));
    }

    @Override
    public void webLog(final Map<String, String> parameters, final boolean failure, final Throwable throwable) throws IOException {
        final ArrayList<MDC.MDCCloseable> closers = new ArrayList<MDC.MDCCloseable>(parameters.size());
        parameters.forEach((k, v) -> closers.add(MDC.putCloseable(k, v)));

        try (final Closeable ignore = new Closer(closers)) {
            if (throwable != null) {
                this.logger.warn(throwable.getMessage(), throwable);
            } else if (failure) {
                this.logger.warn("");
            } else {
                this.logger.info("");
            }
        }
    }

    private static class Closer implements Closeable {
        private final Iterable<MDC.MDCCloseable> closables;
        private Closer(final Iterable<MDC.MDCCloseable> closables) {
            this.closables = closables;
        }

        @Override
        public void close() {
            for (MDC.MDCCloseable closable : closables) {
                closable.close();
            }
        }
    }
}
