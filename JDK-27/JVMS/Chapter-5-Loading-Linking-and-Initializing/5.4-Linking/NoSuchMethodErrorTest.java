/*
 * @test
 * @summary JVMS 5.4.3: resolution of a symbolic reference to a method that does
 *          not exist on the referenced class fails with NoSuchMethodError.
 * @compile NoSuchMethodErrorAccessor.jasm
 * @run main NoSuchMethodErrorTest
 */

public class NoSuchMethodErrorTest {
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
            Class<?> c = Class.forName("NoSuchMethodErrorAccessor");
            c.getMethod("call").invoke(null);
        } catch (Throwable t) {
            threw = causedBy(t, NoSuchMethodError.class);
        }
        check(threw, "missing method resolution rejected with NoSuchMethodError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
