/*
 * @test
 * @summary com.sun.corba.se.impl.dynamicany.DynEnumImpl conformance: type shape via reflection (headless-safe).
 * @run main DynEnumImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DynEnumImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.dynamicany.DynEnumImpl", false, DynEnumImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.dynamicany.DynAnyBasicImpl"), "superclass com.sun.corba.se.impl.dynamicany.DynAnyBasicImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.DynamicAny.DynEnum"), "interface org.omg.DynamicAny.DynEnum");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("component_count"), "method component_count");
        check(mn.contains("current_component"), "method current_component");
        check(mn.contains("get_as_string"), "method get_as_string");
        check(mn.contains("get_as_ulong"), "method get_as_ulong");
        check(mn.contains("set_as_string"), "method set_as_string");
        check(mn.contains("set_as_ulong"), "method set_as_ulong");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
