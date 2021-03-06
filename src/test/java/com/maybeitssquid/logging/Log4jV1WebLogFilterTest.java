package com.maybeitssquid.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class Log4jV1WebLogFilterTest extends AbstractWebLogFilterTest<Logger> {
    public Log4jV1WebLogFilterTest() {
        super(Logger.class);
    }

    @Override
    protected WebLogFilter<Logger> createTestFilter() {
        return new Log4jV1WebLogFilter();
    }

    @Override
    protected String fromContext(String key) {
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
        doAnswer(checkMDC).when(logger).info("");
        super.webLog_parametersSetInMDC();
    }

    @Override
    protected void verifyInfo() {
        verify(logger).info("");
    }

    @Override
    protected void verifyWarn() {
        verify(logger).warn(anyString());
    }

    @Override
    protected void verifyWarnWithThrowable() {
        verify(logger).warn(any(Throwable.class));
    }
}
