/*
 * Copyright (C) 2014 Andrei Varabyeu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.avarabyeu.wills;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.smarttested.qa.smartassert.junit.SoftAssertVerifier;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.smarttested.qa.smartassert.SmartAssert.assertHard;
import static com.smarttested.qa.smartassert.SmartAssert.assertSoft;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

/**
 * Tests {@link com.github.avarabyeu.wills.Will} functionality
 *
 * @author avarabyeu
 */
public class WillsTest {

    private static final String TEST_STRING = "test";

    @Rule
    public SoftAssertVerifier verifier = SoftAssertVerifier.instance();

    @Test
    public void testWill() {
        Will<String> will = Wills.will(TEST_STRING);

        assertSoft(will.isDone(), is(true), "Incorrect Will status");
        assertSoft(will.isCancelled(), is(false), "Incorrect Will status");
        assertSoft(will.obtain(), is(TEST_STRING), "Incorrect Will result");
    }

    @Test
    public void testMap() {
        Will<String> will = Wills.will(TEST_STRING).map(new Function<String, String>() {

            @Nullable
            @Override
            public String apply(@Nullable String input) {
                assert input != null;
                return input.toUpperCase();
            }
        });


        assertHard(will.obtain(), is(TEST_STRING.toUpperCase()), "Incorrect Will Result");
    }

    @Test
    public void testWhenDone() {
        final List<String> results = Lists.newArrayList();
        Will<String> will = Wills.will(TEST_STRING).whenDone(new Action<String>() {
            @Override
            public void apply(String s) {
                results.add(s);
            }
        });
        /* waits for done */
        will.obtain();
        assertHard(results, hasItem(TEST_STRING), "Will whenDone statement is not processed");
    }

    @Test
    public void testWhenFailed() {
        final List<Throwable> results = Lists.newArrayList();
        RuntimeException throwable = new RuntimeException("");
        Will<String> will = Wills.<String>failedWill(throwable).whenFailed(new Action<Throwable>() {
            @Override
            public void apply(Throwable throwable) {
                results.add(throwable);
            }
        });


        try {
            /* waits for done */
            will.obtain();
        } catch (RuntimeException e) {
            Assert.assertThat(e, is(throwable));
        }
        assertHard(results, hasItem(throwable), "Will whenFail statement is not processed");
    }

    @Test
    public void testWhenCompletedSuccessful() {
        final AtomicBoolean result = new AtomicBoolean();
        Wills.will("successful").whenCompleted(new Action<Boolean>() {
            @Override
            public void apply(Boolean aBoolean) {
                result.set(aBoolean);
            }
        });
        assertHard(result.get(), is(true), "Incorrect when completed value");
    }

    @Test
    public void testWhenCompletedFailed() {
        final AtomicBoolean result = new AtomicBoolean(true);
        Wills.failedWill(new RuntimeException()).whenCompleted(new Action<Boolean>() {
            @Override
            public void apply(Boolean aBoolean) {
                result.set(aBoolean);
            }
        });
        assertHard(result.get(), is(false), "Incorrect when completed value");
    }
}
