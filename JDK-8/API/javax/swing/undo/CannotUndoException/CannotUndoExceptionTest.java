/*
 * @test
 * @summary javax.swing.undo.CannotUndoException conformance: exception (headless-safe).
 * @run main CannotUndoExceptionTest
 */

import javax.swing.undo.CannotUndoException;

public class CannotUndoExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        CannotUndoException e = new CannotUndoException();
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
