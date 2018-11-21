package com.maybeitssquid.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract public class AbstractWebLogFilterTest<L> {

    /**
     * The filter under test
     */
    private WebLogFilter<L> test;

    /**
     * Create a filter to test.
     */
    abstract WebLogFilter<L> createTestFilter();

    /**
     * Read a value from the implementation-specific MDC.
     */
    abstract String fromContext(final String key);

    /**
     * Verify that at the time the log is created the MDC contains key1->test1 and key2->test2
     */
    abstract protected void verifyMDCParameters();

    /**
     * Verify that a log entry was created at info level.
     */
    abstract protected void verifyInfo();

    /**
     * Verify that a log entry was created with a non-empty message at warn level.
     */
    abstract protected void verifyWarn();

    /**
     * Create and initialize the test filter.
     */
    @BeforeEach
    protected void init() {
        this.test = createTestFilter();
        test.setLogger("test");
    }

    @Test
    public void setLogger_byStringCreatesLogger() {
        // Clear any logger from test setup
        test.logger = null;
        test.setLogger("testLogger");
        Assertions.assertNotNull(test.logger);
    }

    @Test
    public void webLog_parametersSetInMDC() throws IOException {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key1", "test1");
        parameters.put("key2", "test2");

        test.webLog(parameters, false, null);

        verifyMDCParameters();
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
    }

    @Test
    public void webLog_warnOnExceptionPath() throws IOException {
        test.webLog(Collections.emptyMap(), true, new Throwable("Mock failure"));
        verifyWarn();
    }
}
