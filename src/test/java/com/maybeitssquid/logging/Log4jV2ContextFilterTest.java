package com.maybeitssquid.logging;

import org.junit.jupiter.api.BeforeEach;
import org.apache.logging.log4j.ThreadContext;

import javax.servlet.ServletException;

import static org.mockito.Mockito.spy;

public class Log4jV2ContextFilterTest extends AbstractContextFilterTest {

    @BeforeEach
    public void createFilter() throws ServletException {
        this.test = spy(Log4jV2ContextFilter.class);
        initContextFilter();
    }

    @Override
    String fromContext(String key) {
        final Object result = ThreadContext.get(key);
        return result == null ? null : result.toString();
    }
}
