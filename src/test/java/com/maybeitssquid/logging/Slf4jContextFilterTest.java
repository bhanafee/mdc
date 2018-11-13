package com.maybeitssquid.logging;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;

import javax.servlet.ServletException;

import static org.mockito.Mockito.spy;

public class Slf4jContextFilterTest extends AbstractContextFilterTest {

    @BeforeEach
    public void createFilter() throws ServletException {
        this.test = spy(Slf4jContextFilter.class);
        initContextFilter();
    }

    @Override
    String fromContext(String key) {
        return MDC.get(key);
    }
}
