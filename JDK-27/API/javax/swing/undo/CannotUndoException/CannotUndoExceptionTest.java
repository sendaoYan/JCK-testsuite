/*
 * @test
 * @summary javax.swing.undo.CannotUndoException conformance: white-box structure + black-box behavior.
 * @run main CannotUndoExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.swing.undo.CannotUndoException;

public class CannotUndoExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.CannotUndoException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CannotUndoException e = new CannotUndoException();
        check(e != null, "constructs");
        check(e.getMessage() == null, "no-arg constructor leaves message null");
        check(e instanceof RuntimeException, "is a RuntimeException");
        check(e instanceof Exception, "is an Exception");

        // It is a usable signal: throwable and catchable as its own type.
        try {
            throw new CannotUndoException();
        } catch (CannotUndoException caught) {
            check(caught.getMessage() == null, "caught instance carries no message");
        }

        // It behaves as an unchecked exception; toString names the exception type.
        check(e.toString().startsWith("javax.swing.undo.CannotUndoException"), "toString names the exception type");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        CannotUndoException e = new CannotUndoException();
        check(e instanceof RuntimeException, "is RuntimeException");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
