package injection.bug;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class InjectionBugTest {

  @Inject EmbeddedApplication<?> application;

  @Inject Injectee bean;

  @Test
  void testItWorks() {
    Assertions.assertTrue(application.isRunning());
  }

  @Test
  void testInjectedProvider() {
    Assertions.assertEquals("first -1, second 2, third null", bean.showWhatIsInjected());
    Assertions.assertEquals("first -2, second 4, third null", bean.showWhatIsInjected());
    Assertions.assertEquals("first -3, second 6, third null", bean.showWhatIsInjected());
  }

  @Test
  void testConcurrency() {

    final var rnd = new Random(System.currentTimeMillis());
    assertTrue(
        ForkJoinPool.commonPool()
            .invokeAll(
                IntStream.range(0, 10)
                    .mapToObj(
                        idx ->
                            (Callable<Boolean>)
                                () -> {
                                  // Thread.sleep(rnd.nextLong((idx + 1) * 100));

                                  System.out.println(
                                      idx
                                          + " "
                                          + Thread.currentThread()
                                          + " "
                                          + bean.showWhatIsInjected());
                                  return true;
                                })
                    .collect(Collectors.toList()))
            .parallelStream()
            .allMatch(
                future -> {
                  try {
                    return future.get();
                  } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return false;
                  }
                }));
  }
}
