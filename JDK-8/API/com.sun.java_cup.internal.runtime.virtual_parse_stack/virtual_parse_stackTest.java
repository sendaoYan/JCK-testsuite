/*
 * @test
 * @summary com.sun.java_cup.internal.runtime.virtual_parse_stack conformance: type shape via reflection (headless-safe).
 * @run main virtual_parse_stackTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class virtual_parse_stackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java_cup.internal.runtime.virtual_parse_stack", false, virtual_parse_stackTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java_cup.internal.runtime.virtual_parse_stack"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("get_from_real"), "method get_from_real");
        check(names.contains("empty"), "method empty");
        check(names.contains("top"), "method top");
        check(names.contains("pop"), "method pop");
        check(names.contains("push"), "method push");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
