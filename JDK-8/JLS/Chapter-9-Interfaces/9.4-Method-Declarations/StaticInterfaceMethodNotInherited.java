/*
 * @test
 * @summary JLS 9.4 (Java SE 8): a static interface method is not inherited by a class
 *          that implements the interface; it is invoked only through the interface.
 * @run main StaticInterfaceMethodNotInherited
 */

public class StaticInterfaceMethodNotInherited {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface I {
        static int m() { return 42; }
    }
    static class Impl implements I {}

    public static void main(String[] args) {
        check(I.m() == 42, "static method called through the interface");
        check(!(Impl.class.getDeclaredMethods().length > 0 && hasM(Impl.class)),
              "static interface method not inherited into class");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasM(Class<?> c) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("m")) return true;
        }
        return false;
    }
}
