/*
 * @test
 * @summary java.awt.Dialog.ModalExclusionType conformance: enum reflection.
 * @run main ModalExclusionTypeTest
 */

public class ModalExclusionTypeTest {
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
        Class<?> c = Class.forName("java.awt.Dialog$ModalExclusionType");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "valueOf"), "method valueOf");
        Object[] consts = c.getEnumConstants();
        check(consts != null && consts.length == 3, "3 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : consts) names.add(o.toString());
        check(names.contains("NO_EXCLUDE"), "constant NO_EXCLUDE");
        check(names.contains("APPLICATION_EXCLUDE"), "constant APPLICATION_EXCLUDE");
        check(names.contains("TOOLKIT_EXCLUDE"), "constant TOOLKIT_EXCLUDE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
