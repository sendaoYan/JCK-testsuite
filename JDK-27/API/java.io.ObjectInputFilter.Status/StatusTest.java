/*
 * @test
 * @summary java.io.ObjectInputFilter.Status conformance: enum constants and valueOf/values.
 * @run main StatusTest
 */
import java.io.ObjectInputFilter.Status;

public class StatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Status.class.isEnum(), "is enum");
        check(Status.UNDECIDED != null && Status.ALLOWED != null && Status.REJECTED != null, "constants");
        check(Status.values().length == 3, "values length 3");
        check(Status.valueOf("ALLOWED") == Status.ALLOWED, "valueOf ALLOWED");
        check(Status.UNDECIDED.ordinal() == 0, "UNDECIDED ordinal 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
