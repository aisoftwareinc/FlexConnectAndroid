package com.aisoftware.flexconnect

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor


class AppExecutors constructor() {

    private lateinit var diskIO: Executor
    private lateinit var networkIO: Executor
    private lateinit var mainThread: Executor

    constructor(diskIO: Executor, networkIO: Executor, mainThread: Executor) : this() {
        this.diskIO = diskIO
        this.networkIO = networkIO
        this.mainThread = mainThread
    }

    fun diskIO(): Executor {
        return diskIO
    }

    fun networkIO(): Executor {
        return networkIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

}

class MainThreadExecutor : Executor {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mainThreadHandler.post(command)
    }
}
