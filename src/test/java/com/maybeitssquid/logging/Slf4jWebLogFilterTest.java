package com.maybeitssquid.logging;

import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Slf4jWebLogFilterTest extends AbstractWebLogFilterTest {

    @BeforeEach
    public void createFilter() {
        this.test = new Slf4jWebLogFilter();

        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        this.appender = mock(Appender.class);
        when(appender.getName()).thenReturn("MOCK");
        when(appender.isStarted()).thenReturn(true);
        logger.addAppender(this.appender);
        this.test.setLogger(logger);
    }

    @Override
    String fromContext(String key) {
        return MDC.get(key);
    }
}
