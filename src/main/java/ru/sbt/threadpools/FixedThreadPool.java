package ru.sbt.threadpools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {
    private final int taskCount;
    private ArrayList<Task> threads;
    private LinkedList<Runnable> tasks;
    private volatile boolean isRun = true;
    private final Object threadLock = new Object();

    FixedThreadPool(int taskCount) {
        if(taskCount <= 0)
            throw new IllegalArgumentException("Task's count must be greater then 0");
        this.taskCount = taskCount;
        threads = new ArrayList<>(taskCount);
        tasks = new LinkedList<>();
        for(int i = 0; i < taskCount;i++)
            threads.add(new Task());
    }
    public void start() {
        threads.forEach(Task::start);

    }

    public void interrupt() {
        isRun = false;
        threads.forEach(Task::interrupt);
    }

    public void execute(Runnable runnable) {
        synchronized (threadLock) {
            tasks.add(runnable);
            threadLock.notify();
        }
    }

    private class Task extends Thread {
        @Override
        public void run() {
            while (isRun) {
                final Runnable task;
                synchronized (threadLock) {
                    if (tasks.isEmpty())
                        try {
                            threadLock.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    task = tasks.poll();
                }
                task.run();
            }
        }
    }
}
