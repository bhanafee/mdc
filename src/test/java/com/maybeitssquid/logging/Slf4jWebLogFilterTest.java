package com.maybeitssquid.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class Slf4jWebLogFilterTest extends AbstractWebLogFilterTest<Logger> {

    public Slf4jWebLogFilterTest() {
        super(Logger.class);
    }

    @Override
    protected WebLogFilter<Logger> createTestFilter() {
        return new Slf4jWebLogFilter();
    }

    @Override
    protected String fromContext(String key) {
        return MDC.get(key);
    }


    @Override
    @Test
    public void webLog_parametersSetInMDC() throws IOException {
        Answer checkMDC = (ignore) -> {
            Assertions.assertEquals("test1", MDC.get("key1"));
            Assertions.assertEquals("test2", MDC.get("key2"));
            return null;
        };
        doAnswer(checkMDC).when(logger).info(anyString());
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
        verify(logger).warn(anyString(), any(Throwable.class));
    }
}
