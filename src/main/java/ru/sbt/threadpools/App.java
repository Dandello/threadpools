package ru.sbt.threadpools;
public class App 
{
    public static void main( String[] args ) {
        ThreadPool threads = new ScalableThreadPool(10, 20);
        threads.start();
        threads.execute(() -> System.out.println(123));
        threads.interrupt();
    }

}
