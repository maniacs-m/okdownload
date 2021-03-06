/*
 * Copyright (c) 2017 LingoChamp Inc.
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

package com.liulishuo.okdownload.core.listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;

public abstract class DownloadListener4WithSpeed extends DownloadListener4 {
    private SpeedCalculator taskSpeed;
    private SparseArray<SpeedCalculator> blockSpeeds;

    @NonNull protected SpeedCalculator taskSpeed() {
        if (taskSpeed == null) {
            throw new IllegalAccessError("This method can't be invoked before infoReady");
        }

        return taskSpeed;
    }

    @NonNull protected SpeedCalculator blockSpeed(int blockIndex) {
        if (blockSpeeds == null) {
            throw new IllegalAccessError("This method can't be invoked before infoReady");
        }

        return blockSpeeds.get(blockIndex);
    }

    @Override public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
        taskSpeed.downloading(increaseBytes);
        blockSpeeds.get(blockIndex).downloading(increaseBytes);

        super.fetchProgress(task, blockIndex, increaseBytes);
    }

    @Override public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
        blockSpeeds.get(blockIndex).endTask();
        super.fetchEnd(task, blockIndex, contentLength);
    }

    @Override
    public void taskEnd(DownloadTask task, EndCause cause, @Nullable Exception realCause) {
        taskSpeed.endTask();
        taskEnd(task, cause, realCause, taskSpeed.speedFromBegin());
    }

    protected abstract void taskEnd(DownloadTask task, EndCause cause,
                                    @Nullable Exception realCause,
                                    @NonNull String averageSpeed);

    @Override protected void initData(@NonNull BreakpointInfo info) {
        super.initData(info);
        initSpeed(info);
    }

    private void initSpeed(BreakpointInfo info) {
        taskSpeed = new SpeedCalculator();
        final int blockCount = info.getBlockCount();
        blockSpeeds = new SparseArray<>(blockCount);
        for (int i = 0; i < blockCount; i++) blockSpeeds.put(i, new SpeedCalculator());
    }
}
