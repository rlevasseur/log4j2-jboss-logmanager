/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logmanager.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.jboss.logmanager.formatters.PatternFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ThreadContextMapTestCase extends AbstractTestCase {
    private final org.jboss.logmanager.Logger jbossRootLogger = org.jboss.logmanager.Logger.getLogger("");

    @Before
    public void setup() {
        jbossRootLogger.clearHandlers();
    }

    @Test
    public void testPut() {
        final String key = "test.key";
        final TestQueueHandler handler = new TestQueueHandler(new PatternFormatter("%X{" + key + "}"));
        jbossRootLogger.addHandler(handler);
        ThreadContext.put(key, "test value");

        final Logger logger = LogManager.getLogger();

        logger.info("Test message");

        Assert.assertEquals("test value", handler.pollFormatted());

        ThreadContext.remove(key);

        logger.info("Test message");
        Assert.assertEquals("", handler.pollFormatted());
    }

    @Test
    public void testPush() {
        final TestQueueHandler handler = new TestQueueHandler(new PatternFormatter("%x"));
        jbossRootLogger.addHandler(handler);

        ThreadContext.push("value-1");
        ThreadContext.push("value-2");
        ThreadContext.push("value-3");

        final Logger logger = LogManager.getLogger();

        logger.info("Test message");
        Assert.assertEquals("value-1.value-2.value-3", handler.pollFormatted());

        ThreadContext.trim(2);
        Assert.assertEquals(2, ThreadContext.getDepth());

        logger.info("Test message");
        Assert.assertEquals("value-1.value-2", handler.pollFormatted());
    }

}
