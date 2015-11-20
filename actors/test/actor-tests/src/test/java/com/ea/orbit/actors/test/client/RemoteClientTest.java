/*
 Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

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

package com.ea.orbit.actors.test.client;


import com.ea.orbit.actors.Actor;
import com.ea.orbit.actors.Stage;
import com.ea.orbit.actors.runtime.AbstractActor;
import com.ea.orbit.actors.runtime.RemoteClient;
import com.ea.orbit.actors.test.ActorBaseTest;
import com.ea.orbit.actors.test.actors.SomeActor;
import com.ea.orbit.concurrent.Task;

import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unused")
public class RemoteClientTest extends ActorBaseTest
{
    public interface Hello extends Actor
    {
        Task<String> sayHello(String greeting);
    }

    public static class HelloActor extends AbstractActor implements Hello
    {
        @Override
        public Task<String> sayHello(final String greeting)
        {
            return Task.fromValue(greeting);
        }
    }

    @Test
    public void callServer() throws ExecutionException, InterruptedException
    {
        Stage stage = createStage();

        RemoteClient client = createRemoteClient(stage);
        Hello actor1 = client.getReference(Hello.class, "1000");
        assertEquals("test", actor1.sayHello("test").join());
        dumpMessages();
    }


    @Test
    @Ignore
    public void timeoutTest() throws ExecutionException, InterruptedException
    {
        Stage stage = createStage();
        clock.stop();
        // make sure the actor is there... remove this later
        RemoteClient client = createRemoteClient(stage);
        SomeActor someActor = Actor.getReference(SomeActor.class, "1");
        Task<UUID> res = someActor.getUniqueActivationId(TimeUnit.SECONDS.toNanos(200));
        clock.incrementTimeMillis(TimeUnit.MINUTES.toMillis(60));

        client.cleanup(true);
        assertTrue(res.isDone());
        assertTrue(res.isCompletedExceptionally());
        expectException(() -> res.join());
    }

}