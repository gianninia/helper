package me.giannini.misc.helper.concurrent;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wraps an {@link ExecutorService} so that an action can be performed when threads are forked. The passed {@link ExecutorServiceListener} receives events whenever a task is
 * submitted to the wrapped service. This can be used for example to share thread local state between threads when the {@link ExecutorService} uses a thread pool or similar so that
 * {@link InheritableThreadLocal} doesn't work anymore.
 */
public class ForkListeningExecutorService implements ExecutorService {

  private final ExecutorService wrapped;
  private final ExecutorServiceListener listener;

  /**
   * Constructs the wrapper {@link ExecutorService}.
   *
   * @param wrapped the {@link ExecutorService} to wrap
   * @param listener - the {@link ExecutorServiceListener} receiving the events when the service forks threads
   */
  public ForkListeningExecutorService(final ExecutorService wrapped, final ExecutorServiceListener listener) {
    this.wrapped = wrapped;
    this.listener = listener;
  }

  @Override
  public void execute(final Runnable command) {
    listener.beforeTaskSubmission();
    wrapped.execute(() -> {
      try {
        listener.afterTaskSubmission();
        command.run();
      } finally {
        listener.beforeTaskEnds();
      }
    });
  }

  @Override
  public <T> Future<T> submit(final Callable<T> task) {
    listener.beforeTaskSubmission();
    return wrapped.submit(() -> {
      try {
        listener.afterTaskSubmission();
        return task.call();
      } finally {
        listener.beforeTaskEnds();
      }
    });
  }

  @Override
  public <T> Future<T> submit(final Runnable task, final T result) {
    listener.beforeTaskSubmission();
    return wrapped.submit(() -> {
      try {
        listener.afterTaskSubmission();
        task.run();
      } finally {
        listener.beforeTaskEnds();
      }
    }, result);
  }

  @Override
  public Future<?> submit(final Runnable task) {
    listener.beforeTaskSubmission();
    return wrapped.submit(() -> {
      try {
        listener.afterTaskSubmission();
        task.run();
      } finally {
        listener.beforeTaskEnds();
      }
    });
  }

  @Override
  public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
    listener.beforeTaskSubmission();
    return wrapped.invokeAll(tasks.stream()
        .map(task -> (Callable<T>)() -> {
          try {
            listener.afterTaskSubmission();
            return task.call();
          } finally {
            listener.beforeTaskEnds();
          }
        })
        .collect(toList()));
  }

  @Override
  public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
    listener.beforeTaskSubmission();
    return wrapped.invokeAll(tasks.stream()
        .map(task -> (Callable<T>)() -> {
          try {
            listener.afterTaskSubmission();
            return task.call();
          } finally {
            listener.beforeTaskEnds();
          }
        })
        .collect(toList()), timeout, unit);
  }

  @Override
  public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    listener.beforeTaskSubmission();
    return wrapped.invokeAny(tasks.stream()
        .map(task -> (Callable<T>)() -> {
          try {
            listener.afterTaskSubmission();
            return task.call();
          } finally {
            listener.beforeTaskEnds();
          }
        })
        .collect(toList()));
  }

  @Override
  public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    listener.beforeTaskSubmission();
    return wrapped.invokeAny(tasks.stream()
        .map(task -> (Callable<T>)() -> {
          try {
            listener.afterTaskSubmission();
            return task.call();
          } finally {
            listener.beforeTaskEnds();
          }
        })
        .collect(toList()), timeout, unit);
  }

  @Override
  public void shutdown() {
    wrapped.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return wrapped.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return wrapped.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return wrapped.isTerminated();
  }

  @Override
  public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
    return wrapped.awaitTermination(timeout, unit);
  }

  /**
   * Listener that will receive the events around task submission.
   *
   */
  public interface ExecutorServiceListener {

    /**
     * Will be called <b>before</b> any task is submitted to the service. This method will therefore run on the original "parent" thread.
     */
    default void beforeTaskSubmission() {
      // to be overridden by implementations
    }

    /**
     * Will be called <b>after</b> a task is submitted to the service and <b>before</b> the actual execution starts. This method will therefore run on the new "child" thread.
     */
    default void afterTaskSubmission() {
      // to be overridden by implementations
    }

    /**
     * Will be called <b>before</b> a submitted task ends no matter if an exception was thrown or not. This method will therefore run on the new "child" thread just before it's
     * released.
     */
    default void beforeTaskEnds() {
      // to be overridden by implementations
    }
  }
}
