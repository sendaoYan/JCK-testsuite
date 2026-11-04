/*
 * @test
 * @summary com.sun.beans.decoder.ElementHandler conformance: type shape via reflection (headless-safe).
 * @run main ElementHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ElementHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.beans.decoder.ElementHandler", false, ElementHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.beans.decoder.ElementHandler"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getOwner"), "method getOwner");
        check(names.contains("getParent"), "method getParent");
        check(names.contains("getVariable"), "method getVariable");
        check(names.contains("getContextBean"), "method getContextBean");
        check(names.contains("addAttribute"), "method addAttribute");
        check(names.contains("startElement"), "method startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
