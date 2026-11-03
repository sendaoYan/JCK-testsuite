/*
 * @test
 * @summary java.lang.reflect.InvocationTargetException conformance: white-box structure + black-box behavior.
 * @run main InvocationTargetExceptionTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvocationTargetExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Boom {
        void explode() {
            throw new ArithmeticException("/ by zero-marker");
        }

        void checked() throws java.io.IOException {
            throw new java.io.IOException("checked-boom");
        }
    }

    // subclass exercises the protected no-arg constructor
    static class Sub extends InvocationTargetException {
        Sub() {
            super();
        }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() {
        InvocationTargetException e =
                new InvocationTargetException(new RuntimeException("cause"));
        check(e.getCause() instanceof RuntimeException, "getCause");
        check(e.getTargetException() == e.getCause(), "getTargetException == cause");
        InvocationTargetException e2 =
                new InvocationTargetException(new RuntimeException("cause"), "msg");
        check("msg".equals(e2.getMessage()), "message constructor");
        check(e2.getCause() instanceof RuntimeException, "cause constructor");
        check(java.lang.ReflectiveOperationException.class
                .isAssignableFrom(InvocationTargetException.class), "extends ReflectiveOperationException");
        check(InvocationTargetException.class != null, "public class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. real trigger: Method.invoke wraps target exceptions
        Method explode = Boom.class.getDeclaredMethod("explode");
        try {
            explode.invoke(new Boom());
            check(false, "invoke must wrap");
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof ArithmeticException, "runtime cause exact type");
            check("/ by zero-marker".equals(e.getCause().getMessage()), "cause message");
            check(e.getTargetException() == e.getCause(), "target == cause identity");
        }

        // 2. checked exceptions are wrapped identically
        Method checked = Boom.class.getDeclaredMethod("checked");
        try {
            checked.invoke(new Boom());
            check(false, "checked must wrap");
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof java.io.IOException, "checked cause exact type");
            check("checked-boom".equals(e.getCause().getMessage()), "checked cause message");
        }

        // 3. constructor state machines
        RuntimeException root = new RuntimeException("root");
        InvocationTargetException withMsg = new InvocationTargetException(root, "wrapper-msg");
        check("wrapper-msg".equals(withMsg.getMessage()), "message kept");
        check(withMsg.getCause() == root, "cause kept by reference");
        check(withMsg.getTargetException() == root, "target kept by reference");
        InvocationTargetException bare = new InvocationTargetException(root);
        check(bare.getMessage() == null, "no message when not given");
        check(bare.toString().contains("InvocationTargetException"), "toString names the type");

        // 4. protected no-arg constructor via subclass: cause can be set later
        Sub sub = new Sub();
        check(sub.getCause() == null && sub.getTargetException() == null, "sub no cause");
        check(sub.getMessage() == null, "sub no message");
        try {
            sub.initCause(root);
            check(false, "sub initCause must be refused");
        } catch (IllegalStateException e) {
            check(true, "IllegalStateException sub initCause (cause preset to null)");
        }

        // 5. preset cause cannot be re-initialized
        InvocationTargetException fixed = new InvocationTargetException(root);
        try {
            fixed.initCause(new RuntimeException("other"));
            check(false, "re-init must fail");
        } catch (IllegalStateException e) {
            check(true, "IllegalStateException on re-initCause");
        }

        // 6. exceptions propagate addSuppressed/printStackTrace safely
        InvocationTargetException printed = new InvocationTargetException(root, "print-me");
        printed.addSuppressed(new RuntimeException("sup"));
        check(printed.getSuppressed().length == 1, "suppressed carried");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        printed.printStackTrace(new java.io.PrintStream(bos, true, "UTF-8"));
        check(bos.size() > 0 && bos.toString("UTF-8").contains("root"),
              "stack trace rendered with cause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
