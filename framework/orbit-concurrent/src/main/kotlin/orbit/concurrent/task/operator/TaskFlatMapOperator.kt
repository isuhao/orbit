/*
 Copyright (C) 2015 - 2018 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <http://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.concurrent.task.operator

import orbit.concurrent.task.Task
import orbit.util.tries.Try


internal class TaskFlatMapOperator<I, O>(private val body: (I) -> Task<O>): TaskOperator<I, O>() {
    override fun onFulfilled(result: Try<I>) {
        result.onSuccess {
            try {
                body(it).doAlways {
                    value = it
                    triggerListeners()
                }
            } catch(t: Throwable) {
                value = Try.failed(t)
                triggerListeners()
            }
        }.onFailure {
            value = Try.failed(it)
            triggerListeners()
        }
    }
}