/*
 * @test
 * @summary javax.swing.LayoutStyle.ComponentPlacement conformance: type shape via reflection (headless-safe).
 * @run main ComponentPlacementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ComponentPlacementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.LayoutStyle$ComponentPlacement", false, ComponentPlacementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        Object[] en = c.getEnumConstants();
        check(en != null && en.length == 3, "enum constant count");
        check(en != null && en.length > 0 && "RELATED".equals(((Enum) en[0]).name()), "const RELATED");
        check(en != null && en.length > 1 && "UNRELATED".equals(((Enum) en[1]).name()), "const UNRELATED");
        check(en != null && en.length > 2 && "INDENT".equals(((Enum) en[2]).name()), "const INDENT");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "method values");
        check(names.contains("valueOf"), "method valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
