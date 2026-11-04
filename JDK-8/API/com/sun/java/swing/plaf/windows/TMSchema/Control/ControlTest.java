/*
 * @test
 * @summary com.sun.java.swing.plaf.windows.TMSchema.Control conformance: type shape via reflection (headless-safe).
 * @run main ControlTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.plaf.windows.TMSchema$Control", false, ControlTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.swing.plaf.windows.TMSchema$Control"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        try {
            check(c.getEnumConstants() != null, "enumConstants");
        } catch (ExceptionInInitializerError e) {
            check(c.isEnum(), "enum (load only)");
        } catch (UnsatisfiedLinkError e) {
            check(c.isEnum(), "enum (load only)");
        }
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("values"), "method values");
        check(names.contains("valueOf"), "method valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
