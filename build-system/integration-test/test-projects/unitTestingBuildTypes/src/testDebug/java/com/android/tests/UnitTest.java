/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Ignore;
import android.app.Application;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import org.junit.Test;

public class UnitTest {
    @Test
    public void referenceProductionCode() {
        // Reference code for the tested build type.
        DebugOnlyClass foo = new DebugOnlyClass();
        assertEquals("debug", foo.foo());
    }
}
