/*
 * @test
 * @summary java.util.concurrent.StructuredTaskScope.Configuration conformance:
 *          interface and fluent with* methods via open(UnaryOperator).
 * @run main ConfigurationTest
 * @enablePreview
 */

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class ConfigurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(StructuredTaskScope.Configuration.class.isInterface(),
              "Configuration is an interface");

        final StructuredTaskScope.Configuration[] seen =
                new StructuredTaskScope.Configuration[1];
        try (StructuredTaskScope<String, Void, ExecutionException> scope =
                 StructuredTaskScope.open(cfg -> {
                     seen[0] = cfg.withName("test")
                                  .withTimeout(Duration.ofSeconds(5))
                                  .withThreadFactory(r -> new Thread(r));
                     return seen[0];
                 })) {
            scope.fork(() -> "x");
            scope.join();
        }
        check(seen[0] != null, "withName/withTimeout/withThreadFactory fluent chain");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
