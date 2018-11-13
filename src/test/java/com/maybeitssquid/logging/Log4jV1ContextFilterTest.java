package com.maybeitssquid.logging;

import org.junit.jupiter.api.BeforeEach;
import org.apache.log4j.MDC;

import javax.servlet.ServletException;

import static org.mockito.Mockito.spy;

public class Log4jV1ContextFilterTest extends AbstractContextFilterTest {

    @BeforeEach
    public void createFilter() throws ServletException {
        this.test = spy(Log4jV1ContextFilter.class);
        initContextFilter();
    }

    @Override
    String fromContext(String key) {
        final Object result = MDC.get(key);
        return result == null ? null : result.toString();
    }
}
