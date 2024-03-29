/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.common.resources.configuration;

import junit.framework.TestCase;

public class LanguageQualifierTest extends TestCase {

    private FolderConfiguration config;
    private LanguageQualifier lq;

    @Override
    public void setUp()  throws Exception {
        super.setUp();
        config = new FolderConfiguration();
        lq = new LanguageQualifier();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        config = null;
        lq = null;
    }

    public void testCheckAndSet() {
        assertEquals(true, lq.checkAndSet("en", config)); //$NON-NLS-1$
        assertTrue(config.getLanguageQualifier() != null);
        assertEquals("en", config.getLanguageQualifier().toString()); //$NON-NLS-1$
        //noinspection ConstantConditions
        assertEquals("en", config.getEffectiveLanguage().toString()); //$NON-NLS-1$
    }

    public void testCheckAndSetCaseInsensitive() {
        assertEquals(true, lq.checkAndSet("EN", config)); //$NON-NLS-1$
        assertTrue(config.getLanguageQualifier() != null);
        assertEquals("en", config.getLanguageQualifier().toString()); //$NON-NLS-1$
        assertEquals("en", LanguageQualifier.getFolderSegment("EN"));
    }

    public void testFailures() {
        assertEquals(false, lq.checkAndSet("", config)); //$NON-NLS-1$
        assertEquals(false, lq.checkAndSet("abc", config)); //$NON-NLS-1$
    }
}

