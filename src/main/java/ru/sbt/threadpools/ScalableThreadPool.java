package ru.sbt.threadpools;

import java.util.ArrayList;
import java.util.LinkedList;

public class ScalableThreadPool implements ThreadPool{
    private int minPoolCount;
    private int maxPoolCount;
    private volatile Integer busyPoolCount = 0;
    private ArrayList<Task> threads;
    private LinkedList<Runnable> tasks;
    private volatile boolean isRun = true;
    private final Object threadLock = new Object();

    ScalableThreadPool(int min, int max) {
        if(min > max || min <= 0 || max <= 0)
            throw new IllegalArgumentException("Wrong parameters of scalable pool");
        this.minPoolCount = min;
        this.maxPoolCount = max;
        threads = new ArrayList<>(minPoolCount);
        tasks = new LinkedList<>();
    }

    public void start() {
        threads.forEach(Thread::start);
    }

    public void execute(Runnable runnable) {
        synchronized (threadLock) {
            tasks.add(runnable);
            if(busyPoolCount >= threads.size()) {
                Task thread = new Task();
                threads.add(thread);
                thread.run();
            }
            threadLock.notify();
        }
    }
    public void interrupt() {
        isRun = false;
        threads.forEach(Task::interrupt);
    }

    private class Task extends Thread {
        @Override
        public void run() {
            while(isRun) {
                final Runnable task;
                synchronized (threadLock) {
                    if(tasks.isEmpty()) {
                        if (threads.size() > minPoolCount)
                            break;
                        try {
                            threadLock.wait();
                        } catch(InterruptedException e) {
                            interrupt();
                            break;
                        }
                    }
                    task = tasks.poll();
                }
                synchronized (busyPoolCount) {
                    busyPoolCount++;
                }
                task.run();
                synchronized (busyPoolCount) {
                    busyPoolCount--;
                }
            }
        }
    }
}
