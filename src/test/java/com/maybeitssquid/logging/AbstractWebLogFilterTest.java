package com.maybeitssquid.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

abstract public class AbstractWebLogFilterTest<L> {

    /**
     * The filter under test
     */
    private WebLogFilter<L> test;

    /**
     * The mock logger used by the test filter
     */
    protected L logger;

    /**
     * Used to initialize the mock logger
     */
    private final Class<L> loggerClass;

    public AbstractWebLogFilterTest(final Class<L> loggerClass) {
        this.loggerClass = loggerClass;
    }

    /**
     * Create and initialize the test filter.
     */
    @BeforeEach
    protected void init() {
        this.logger = mock(this.loggerClass);
        this.test = createTestFilter();
        this.test.logger = logger;
    }

    /**
     * Create a filter to test.
     */
    abstract protected WebLogFilter<L> createTestFilter();

    /**
     * Read a value from the implementation-specific MDC.
     */
    abstract protected String fromContext(final String key);

    /**
     * Verify that a log entry was created at info level.
     */
    abstract protected void verifyInfo();

    /**
     * Verify that a log entry was created with a non-empty message at warn level.
     */
    abstract protected void verifyWarn();

    /**
     * Verify that a log entry was created with a non-empty message and a throwable at warn level.
     */
    abstract protected void verifyWarnWithThrowable();

    @Test
    public void setLogger_byStringCreatesLogger() {
        final L original = test.logger;
        test.setLogger("testLogger");
        Assertions.assertNotSame(original, test.logger);
    }

    /**
     * Override this test to assert MDC parameters are set within call.
     *
     * @throws IOException
     */
    @Test
    public void webLog_parametersSetInMDC() throws IOException {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key1", "test1");
        parameters.put("key2", "test2");

        test.webLog(parameters, false, null);
    }

    @Test
    public void webLog_MDCCleared() throws IOException {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key1", "test1");
        parameters.put("key2", "test2");

        test.webLog(parameters, false, null);

        Assertions.assertNull(fromContext("key1"));
        Assertions.assertNull(fromContext("key2"));
    }

    @Test
    public void webLog_infoOnSuccessPath() throws IOException {
        test.webLog(Collections.emptyMap(), false, null);
        verifyInfo();
    }

    @Test
    public void webLog_warnOnFailurePath() throws IOException {
        test.webLog(Collections.emptyMap(), true, null);
        verifyWarn();
    }

    @Test
    public void webLog_warnOnExceptionPath() throws IOException {
        test.webLog(Collections.emptyMap(), true, new Throwable("Mock failure"));
        verifyWarnWithThrowable();
    }
}
