/*
 * @test
 * @summary javax.swing.plaf.multi.MultiListUI conformance: type shape via reflection (headless-safe).
 * @run main MultiListUITest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MultiListUITest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.multi.MultiListUI", false, MultiListUITest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.ListUI"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUIs"), "method getUIs");
        check(names.contains("locationToIndex"), "method locationToIndex");
        check(names.contains("indexToLocation"), "method indexToLocation");
        check(names.contains("getCellBounds"), "method getCellBounds");
        check(names.contains("contains"), "method contains");
        check(names.contains("update"), "method update");
        check(names.contains("createUI"), "method createUI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
