/**
 * Copyright (C) 2011 Ray Message
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerThread extends Thread {

  private final List<Runnable> workQueue;

    private ExecutorService executorService = Executors.newCachedThreadPool();

  public WorkerThread(List<Runnable> workQueue, String name) {
    super(name);
    this.workQueue = workQueue;
  }

  private Runnable getWork() {
    synchronized (workQueue) {
      try {
        while (workQueue.isEmpty())
          workQueue.wait();

        return workQueue.remove(0);
      } catch (InterruptedException ie) {
        throw new AssertionError(ie);
      }
    }
  }

  @Override
  public void run() {
    for (;;)
        executorService.submit(getWork());
//      getWork().run();
  }
}
