/*
 * @test
 * @summary JVMS 6.5: invokedynamic links a call site through its bootstrap
 *          method and invokes the target method handle.
 * @compile IndyBootstrap.java
 * @compile IndyOps.jasm
 * @run main IndyOpsTest
 */

public class IndyOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check("indy".equals(IndyOps.call()), "invokedynamic bootstrap -> target");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
