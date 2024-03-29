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

public class RegionQualifierTest extends TestCase {

    private RegionQualifier rq;
    private FolderConfiguration config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        rq = new RegionQualifier();
        config = new FolderConfiguration();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        rq = null;
        config = null;
    }

    public void testCheckAndSet() {
        assertEquals(true, rq.checkAndSet("rUS", config));//$NON-NLS-1$
        assertTrue(config.getRegionQualifier() != null);
        assertEquals("US", config.getRegionQualifier().getValue()); //$NON-NLS-1$
        //noinspection ConstantConditions
        assertEquals("US", config.getEffectiveRegion().getValue()); //$NON-NLS-1$
        assertEquals("rUS", config.getRegionQualifier().toString()); //$NON-NLS-1$
        assertEquals("rUS", config.getRegionQualifier().toString()); //$NON-NLS-1$
    }

    public void testCheckCaseInsensitive() {
        assertEquals(true, rq.checkAndSet("rus", config));//$NON-NLS-1$
        assertTrue(config.getRegionQualifier() != null);
        assertEquals("US", config.getRegionQualifier().getValue()); //$NON-NLS-1$
        assertEquals("rUS", config.getRegionQualifier().toString()); //$NON-NLS-1$
        assertEquals("rUS", config.getRegionQualifier().toString()); //$NON-NLS-1$
        assertEquals("rUS", RegionQualifier.getFolderSegment("us"));
    }

    public void testFailures() {
        assertEquals(false, rq.checkAndSet("", config));//$NON-NLS-1$
        assertEquals(false, rq.checkAndSet("rUSA", config));//$NON-NLS-1$
        assertEquals(false, rq.checkAndSet("abc", config));//$NON-NLS-1$
    }
}
