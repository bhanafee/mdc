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
    public void webLog(final Map<String, String> parameters, final boolean failure, final String message) throws IOException {
        final String m = message == null || message.isEmpty() ? "" : message;

        final ArrayList<MDC.MDCCloseable> closers = new ArrayList<MDC.MDCCloseable>(parameters.size());
        parameters.forEach((k, v) -> closers.add(MDC.putCloseable(k, v)));

        try (final Closeable ignore = new Closer(closers)) {
            if (failure) {
                this.logger.warn(m);
            } else {
                this.logger.info(m);
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
