/*
 * @test
 * @summary com.sun.java.browser.dom.DOMServiceProvider conformance: type shape via reflection (headless-safe).
 * @run main DOMServiceProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DOMServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.browser.dom.DOMServiceProvider", false, DOMServiceProviderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.browser.dom.DOMServiceProvider"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("canHandle"), "method canHandle");
        check(names.contains("getDocument"), "method getDocument");
        check(names.contains("getDOMImplementation"), "method getDOMImplementation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
