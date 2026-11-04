/*
 * @test
 * @summary java.awt.desktop.QuitStrategy conformance: white-box structure + black-box behavior.
 * @run main QuitStrategyTest
 */

import java.lang.reflect.*;

public class QuitStrategyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.desktop.QuitStrategy", false, QuitStrategyTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.desktop.QuitStrategy");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isEnum(), "enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 2, "two constants");
    }

    static void testBlackBox() throws Exception {
        java.awt.desktop.QuitStrategy[] vals = java.awt.desktop.QuitStrategy.values();
        check(vals.length == 2, "values length 2");
        check(vals[0] == java.awt.desktop.QuitStrategy.NORMAL_EXIT, "NORMAL_EXIT first");
        check(vals[1] == java.awt.desktop.QuitStrategy.CLOSE_ALL_WINDOWS, "CLOSE_ALL_WINDOWS second");
        check(java.awt.desktop.QuitStrategy.valueOf("NORMAL_EXIT") == java.awt.desktop.QuitStrategy.NORMAL_EXIT, "valueOf");
        check(java.awt.desktop.QuitStrategy.NORMAL_EXIT.name().equals("NORMAL_EXIT"), "name");
        check(java.awt.desktop.QuitStrategy.NORMAL_EXIT.ordinal() == 0, "ordinal 0");
        check(java.lang.Enum.class.isInstance(java.awt.desktop.QuitStrategy.NORMAL_EXIT), "instanceof Enum");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
