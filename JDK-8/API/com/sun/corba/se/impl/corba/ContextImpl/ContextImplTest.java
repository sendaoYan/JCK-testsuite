/*
 * @test
 * @summary com.sun.corba.se.impl.corba.ContextImpl conformance: type shape via reflection (headless-safe).
 * @run main ContextImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ContextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.corba.ContextImpl", false, ContextImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.Context"), "superclass org.omg.CORBA.Context");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("context_name"), "method context_name");
        check(mn.contains("create_child"), "method create_child");
        check(mn.contains("delete_values"), "method delete_values");
        check(mn.contains("get_values"), "method get_values");
        check(mn.contains("parent"), "method parent");
        check(mn.contains("set_one_value"), "method set_one_value");
        check(mn.contains("set_values"), "method set_values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
