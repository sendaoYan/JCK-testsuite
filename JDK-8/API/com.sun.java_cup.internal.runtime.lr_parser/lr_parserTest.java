/*
 * @test
 * @summary com.sun.java_cup.internal.runtime.lr_parser conformance: type shape via reflection (headless-safe).
 * @run main lr_parserTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class lr_parserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java_cup.internal.runtime.lr_parser", false, lr_parserTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java_cup.internal.runtime.lr_parser"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("error_sync_size"), "method error_sync_size");
        check(names.contains("production_table"), "method production_table");
        check(names.contains("action_table"), "method action_table");
        check(names.contains("reduce_table"), "method reduce_table");
        check(names.contains("start_state"), "method start_state");
        check(names.contains("start_production"), "method start_production");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
