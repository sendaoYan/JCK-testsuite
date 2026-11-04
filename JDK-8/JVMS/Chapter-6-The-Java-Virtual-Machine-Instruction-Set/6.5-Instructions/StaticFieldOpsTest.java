/*
 * @test
 * @summary JVMS 6.5: getstatic/putstatic read and write static class fields
 *          after field resolution.
 * @compile StaticFieldHolder.java
 * @compile StaticFieldOps.jasm
 * @run main StaticFieldOpsTest
 */

public class StaticFieldOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        StaticFieldHolder.counter = 5;
        StaticFieldHolder.name = "init";

        check(StaticFieldOps.getCounter() == 5, "getstatic int");
        check(StaticFieldOps.incrCounter(3) == 8, "getstatic/add/putstatic returns new value");
        check(StaticFieldHolder.counter == 8, "putstatic persisted");

        check("init".equals(StaticFieldOps.getName()), "getstatic reference");
        StaticFieldOps.setName("done");
        check("done".equals(StaticFieldHolder.name), "putstatic reference");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
