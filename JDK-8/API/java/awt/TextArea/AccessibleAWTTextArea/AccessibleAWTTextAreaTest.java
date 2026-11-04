/*
 * @test
 * @summary java.awt.TextArea.AccessibleAWTTextArea conformance: class reflection.
 * @run main AccessibleAWTTextAreaTest
 */

public class AccessibleAWTTextAreaTest {
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
        Class<?> c = Class.forName("java.awt.TextArea$AccessibleAWTTextArea");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.TextComponent$AccessibleAWTTextComponent"), "superclass java.awt.TextComponent$AccessibleAWTTextComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
