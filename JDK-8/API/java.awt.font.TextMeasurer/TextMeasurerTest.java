/*
 * @test
 * @summary java.awt.font.TextMeasurer conformance: class reflection.
 * @run main TextMeasurerTest
 */

public class TextMeasurerTest {
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
        Class<?> c = Class.forName("java.awt.font.TextMeasurer");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getLineBreakIndex"), "method getLineBreakIndex");
        check(hasMethod(c, "getAdvanceBetween"), "method getAdvanceBetween");
        check(hasMethod(c, "getLayout"), "method getLayout");
        check(hasMethod(c, "insertChar"), "method insertChar");
        check(hasMethod(c, "deleteChar"), "method deleteChar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
