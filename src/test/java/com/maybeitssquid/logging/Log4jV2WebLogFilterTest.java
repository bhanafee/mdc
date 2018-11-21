package com.maybeitssquid.logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class Log4jV2WebLogFilterTest extends AbstractWebLogFilterTest<Logger> {

    public Log4jV2WebLogFilterTest() {
        super(Logger.class);
    }

    @Override
    protected WebLogFilter<Logger> createTestFilter() {
        return new Log4jV2WebLogFilter();
    }

    @Override
    protected String fromContext(String key) {
        return ThreadContext.get(key);
    }


    @Override
    @Test
    public void webLog_parametersSetInMDC() throws IOException {
        Answer checkMDC = (ignore) -> {
            Assertions.assertEquals("test1", ThreadContext.get("key1"));
            Assertions.assertEquals("test2", ThreadContext.get("key2"));
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
