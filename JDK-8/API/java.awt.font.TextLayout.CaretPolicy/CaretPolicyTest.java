/*
 * @test
 * @summary java.awt.font.TextLayout.CaretPolicy conformance: class reflection.
 * @run main CaretPolicyTest
 */

public class CaretPolicyTest {
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
        Class<?> c = Class.forName("java.awt.font.TextLayout$CaretPolicy");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getStrongCaret"), "method getStrongCaret");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
