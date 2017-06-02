package ru.sbt.threadpools;

public interface ThreadPool {
    void interrupt();
    void start();
    void execute(Runnable runnable);
}
