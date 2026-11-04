/*
 * @test
 * @summary JVMS 5.4.4: resolution enforces access control, so a symbolic
 *          reference to a private field of a different class fails with
 *          IllegalAccessError.
 * @compile IllegalAccessErrorTarget.java IllegalAccessErrorAccessor.jasm
 * @run main IllegalAccessErrorTest
 */

public class IllegalAccessErrorTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean causedBy(Throwable t, Class<? extends Throwable> cls) {
        for (Throwable x = t; x != null; x = x.getCause())
            if (cls.isInstance(x)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try {
            Class<?> c = Class.forName("IllegalAccessErrorAccessor");
            c.getMethod("read").invoke(null);
        } catch (Throwable t) {
            threw = causedBy(t, IllegalAccessError.class);
        }
        check(threw, "private field access across classes rejected with IllegalAccessError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
