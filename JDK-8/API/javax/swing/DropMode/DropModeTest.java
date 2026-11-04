/*
 * @test
 * @summary javax.swing.DropMode conformance: type shape via reflection (headless-safe).
 * @run main DropModeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DropModeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.DropMode", false, DropModeTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        Object[] en = c.getEnumConstants();
        check(en != null && en.length == 8, "enum constant count");
        check(en != null && en.length > 0 && "USE_SELECTION".equals(((Enum) en[0]).name()), "const USE_SELECTION");
        check(en != null && en.length > 1 && "ON".equals(((Enum) en[1]).name()), "const ON");
        check(en != null && en.length > 2 && "INSERT".equals(((Enum) en[2]).name()), "const INSERT");
        check(en != null && en.length > 3 && "INSERT_ROWS".equals(((Enum) en[3]).name()), "const INSERT_ROWS");
        check(en != null && en.length > 4 && "INSERT_COLS".equals(((Enum) en[4]).name()), "const INSERT_COLS");
        check(en != null && en.length > 5 && "ON_OR_INSERT".equals(((Enum) en[5]).name()), "const ON_OR_INSERT");
        check(en != null && en.length > 6 && "ON_OR_INSERT_ROWS".equals(((Enum) en[6]).name()), "const ON_OR_INSERT_ROWS");
        check(en != null && en.length > 7 && "ON_OR_INSERT_COLS".equals(((Enum) en[7]).name()), "const ON_OR_INSERT_COLS");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "method values");
        check(names.contains("valueOf"), "method valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
