/*
 * @test
 * @summary java.awt.im.InputSubset conformance: class reflection.
 * @run main InputSubsetTest
 */

public class InputSubsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.im.InputSubset");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Character$Subset"), "superclass java.lang.Character$Subset");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
