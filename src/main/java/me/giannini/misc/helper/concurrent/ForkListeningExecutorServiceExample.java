package me.giannini.misc.helper.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.giannini.misc.helper.concurrent.ForkListeningExecutorService.ExecutorServiceListener;

public class ForkListeningExecutorServiceExample {

  private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

  private static void printThreadMessage(final String message) {
    System.out.println(message
        + ", current thread: " + Thread.currentThread().getName()
        + ", value from threadLocal: " + threadLocal.get());
  }

  public static void main(final String[] args) throws Exception {

    threadLocal.set("MY_STATE");

    final ExecutorService executorService = new ForkListeningExecutorService(
        Executors.newCachedThreadPool(),
        new ExecutorServiceListener() {

          private String valueToShare;

          @Override
          public void beforeTaskSubmission() {
            valueToShare = threadLocal.get();
            printThreadMessage("The task is about to be submitted");
          }

          @Override
          public void afterTaskSubmission() {
            threadLocal.set(valueToShare);
            printThreadMessage("The task has been submitted and will start now");
          }

          @Override
          public void beforeTaskEnds() {
            threadLocal.set(null);
            printThreadMessage("The task has finished and thread will be released now");
          }
        });

    executorService.submit(() -> {
      printThreadMessage("The task is running now");
    }).get();

    printThreadMessage("We are back on the main thread");
  }
}
