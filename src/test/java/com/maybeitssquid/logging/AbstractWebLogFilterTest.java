package com.maybeitssquid.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

abstract public class AbstractWebLogFilterTest<L>  {

    protected WebLogFilter test;

    protected Appender<ILoggingEvent> appender;

    abstract String fromContext(final String key);

    @Test
    public void setLogger_byStringCreatesLogger() {
        test.setLogger((Logger) null);
        test.setLogger("testLogger");
        Assertions.assertNotNull(test.logger);
    }

    @Test
    public void webLog_parametersSetInMDC() throws IOException {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key1", "test1");
        parameters.put("key2", "test2");

        test.webLog(parameters, false, null);

        verify(this.appender).doAppend(argThat(arg ->
                arg.getMDCPropertyMap().get("key1").equals("test1") &&
                        arg.getMDCPropertyMap().get("key2").equals("test2")));
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
        verify(this.appender).doAppend(argThat(arg ->
                INFO.equals(arg.getLevel()) && arg.getMessage().isEmpty()));
    }

    @Test
    public void webLog_warnOnFailurePath() throws IOException {
        test.webLog(Collections.emptyMap(), true, null);
        verify(this.appender).doAppend(argThat(arg ->
                WARN.equals(arg.getLevel()) && arg.getMessage().isEmpty()));
    }

    @Test
    public void webLog_warnOnExceptionPath() throws IOException {
        test.webLog(Collections.emptyMap(), true, new Throwable("Mock failure"));
        verify(this.appender).doAppend(argThat(arg ->
                WARN.equals(arg.getLevel()) && !arg.getMessage().isEmpty()));
    }
}
