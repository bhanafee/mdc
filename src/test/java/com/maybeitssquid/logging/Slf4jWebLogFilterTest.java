package com.maybeitssquid.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class Slf4jWebLogFilterTest extends AbstractWebLogFilterTest<Logger> {

    @SuppressWarnings("unchecked")
    private static Appender<ILoggingEvent> appender = mock(Appender.class);

    @BeforeAll
    public static void configureMockBackend() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger(ROOT_LOGGER_NAME).addAppender(appender);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void resetAppender() {
        reset(appender);
    }

    @Override
    WebLogFilter<Logger> createTestFilter() {
        return new Slf4jWebLogFilter();
    }

    @Override
    String fromContext(String key) {
        return MDC.get(key);
    }


    @Override
    protected void verifyMDCParameters() {
        verify(appender).doAppend(argThat(arg ->
                arg.getMDCPropertyMap() != null &&
                        "test1".equals(arg.getMDCPropertyMap().get("key1")) &&
                        "test2".equals(arg.getMDCPropertyMap().get("key2"))));
    }

    @Override
    protected void verifyInfo() {
        verify(appender).doAppend(argThat(arg -> INFO.equals(arg.getLevel())));
    }

    @Override
    protected void verifyWarn() {
        verify(appender).doAppend(argThat(arg ->
                WARN.equals(arg.getLevel()) && arg.getMessage() != null && !arg.getMessage().isEmpty()));
    }
}
