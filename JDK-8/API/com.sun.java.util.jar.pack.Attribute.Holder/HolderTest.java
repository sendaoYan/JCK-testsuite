/*
 * @test
 * @summary com.sun.java.util.jar.pack.Attribute.Holder conformance: type shape via reflection (headless-safe).
 * @run main HolderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class HolderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.util.jar.pack.Attribute$Holder", false, HolderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.util.jar.pack.Attribute$Holder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getCPMap"), "method getCPMap");
        check(names.contains("attributeSize"), "method attributeSize");
        check(names.contains("trimToSize"), "method trimToSize");
        check(names.contains("addAttribute"), "method addAttribute");
        check(names.contains("removeAttribute"), "method removeAttribute");
        check(names.contains("getAttribute"), "method getAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
