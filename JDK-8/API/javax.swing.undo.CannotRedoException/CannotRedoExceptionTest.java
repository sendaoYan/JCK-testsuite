/*
 * @test
 * @summary javax.swing.undo.CannotRedoException conformance: exception (headless-safe).
 * @run main CannotRedoExceptionTest
 */

import javax.swing.undo.CannotRedoException;

public class CannotRedoExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        CannotRedoException e = new CannotRedoException();
        check(e instanceof RuntimeException, "is RuntimeException");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
