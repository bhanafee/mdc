package com.maybeitssquid.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.apache.log4j.Level.INFO;
import static org.apache.log4j.Level.WARN;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class Log4jV1WebLogFilterTest extends AbstractWebLogFilterTest<Logger> {

    private static Appender appender = mock(Appender.class);

    @BeforeAll
    public static void configureMockBackend() {
        Logger.getRootLogger().addAppender(appender);
    }

    @BeforeEach
    public void resetAppender() {
        reset(appender);
    }

    @Override
    WebLogFilter<Logger> createTestFilter() {
        return new Log4jV1WebLogFilter();
    }

    @Override
    String fromContext(String key) {
        final Object result = MDC.get(key);
        return result == null ? null : result.toString();
    }

    @Override
    @Test
    public void webLog_parametersSetInMDC() throws IOException {
        Answer checkMDC = (ignore) -> {
            Assertions.assertEquals("test1", MDC.get("key1"));
            Assertions.assertEquals("test2", MDC.get("key2"));
            return null;
        };
        doAnswer(checkMDC).when(appender).doAppend(any(LoggingEvent.class));
        super.webLog_parametersSetInMDC();
    }

    @Override
    protected void verifyMDCParameters() {
        // EMPTY, at the time this method is called MDC has been cleared already
    }

    @Override
    protected void verifyInfo() {
        verify(appender).doAppend(argThat(arg -> INFO.equals(arg.getLevel())));
    }

    @Override
    protected void verifyWarn() {
        verify(appender).doAppend(argThat(arg ->
                WARN.equals(arg.getLevel()) && arg.getMessage() != null && !arg.getMessage().toString().isEmpty()));
    }
}
