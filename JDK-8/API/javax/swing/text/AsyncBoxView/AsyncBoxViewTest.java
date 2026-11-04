/*
 * @test
 * @summary javax.swing.text.AsyncBoxView conformance: type shape via reflection (headless-safe).
 * @run main AsyncBoxViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AsyncBoxViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AsyncBoxView", false, AsyncBoxViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMajorAxis"), "method getMajorAxis");
        check(names.contains("getMinorAxis"), "method getMinorAxis");
        check(names.contains("getTopInset"), "method getTopInset");
        check(names.contains("setTopInset"), "method setTopInset");
        check(names.contains("getBottomInset"), "method getBottomInset");
        check(names.contains("setBottomInset"), "method setBottomInset");
        check(names.contains("getLeftInset"), "method getLeftInset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
