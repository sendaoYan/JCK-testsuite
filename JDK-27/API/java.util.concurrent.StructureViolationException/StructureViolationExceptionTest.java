/*
 * @test
 * @summary java.util.concurrent.StructureViolationException conformance:
 *          constructors and superclass.
 * @run main StructureViolationExceptionTest
 * @enablePreview
 */

import java.util.concurrent.StructureViolationException;

public class StructureViolationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(RuntimeException.class.isAssignableFrom(StructureViolationException.class),
              "extends RuntimeException");
        check(!StructureViolationException.class.isInterface(),
              "StructureViolationException is a final class");

        StructureViolationException e1 = new StructureViolationException();
        check(e1 != null, "no-arg constructor");

        StructureViolationException e2 = new StructureViolationException("boom");
        check("boom".equals(e2.getMessage()), "String constructor message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
