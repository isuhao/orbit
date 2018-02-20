/*
 Copyright (C) 2018 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package orbit.concurrent

import orbit.concurrent.job.JobManagers
import orbit.concurrent.pipeline.Pipeline
import orbit.concurrent.task.Promise
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class PipelineTest {
    @Test
    fun testDoAlways() {
        val counter = AtomicInteger(0)

        val pipeline =
                Pipeline.create<Int>().doAlways {
                    counter.incrementAndGet()
                }

        pipeline.sinkError(RuntimeException())
        pipeline.sinkValue(5)
        Assertions.assertEquals(2, counter.get())
    }

    @Test
    fun testDoOnValue() {
        val counter = AtomicInteger(0)

        val pipeline =
                Pipeline.create<Int>().doOnValue {
                    counter.incrementAndGet()
                }

        pipeline.sinkError(RuntimeException())
        pipeline.sinkValue(5)
        Assertions.assertEquals(1, counter.get())
    }

    @Test
    fun testDoOnError() {
        val counter = AtomicInteger(0)

        val pipeline =
                Pipeline.create<Int>().doOnError {
                    counter.incrementAndGet()
                }

        pipeline.sinkError(RuntimeException())
        pipeline.sinkValue(5)
        Assertions.assertEquals(1, counter.get())
    }

    @Test
    fun testMap() {
        val successPipeline =
                Pipeline.create<Int>().map {
                    it * it
                }.doOnValue {
                    Assertions.assertEquals(25, it)
                }
        successPipeline.sinkValue(5)

        val initialFailPipeline =
                Pipeline.create<Int>().map {
                    it * it
                }.doOnValue {
                    Assertions.fail("doOnValueRan - Should have failed.")
                }
        initialFailPipeline.sinkError(RuntimeException())

        val duringMapFailPipeline =
                Pipeline.create<Int>().map {
                    it * it
                }.doOnValue {
                    Assertions.fail("doOnValueRan - Should have failed.")
                }
        duringMapFailPipeline.sinkError(RuntimeException())
    }

    @Test
    fun testRunOn() {
        val latch = CountDownLatch(1)
        val specialValue = ThreadLocal<Int>()
        specialValue.set(0)

        val pipeline =
                Pipeline.create<Int>().runOn {
                    JobManagers.parallel()
                }.doOnValue {
                    specialValue.set(it)
                    latch.countDown()
                }

        pipeline.sinkValue(42)
        latch.await()
        Assertions.assertEquals(0, specialValue.get())
    }
}