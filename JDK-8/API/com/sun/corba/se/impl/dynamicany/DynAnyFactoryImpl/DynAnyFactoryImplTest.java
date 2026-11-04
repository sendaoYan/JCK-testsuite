/*
 * @test
 * @summary com.sun.corba.se.impl.dynamicany.DynAnyFactoryImpl conformance: type shape via reflection (headless-safe).
 * @run main DynAnyFactoryImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DynAnyFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.dynamicany.DynAnyFactoryImpl", false, DynAnyFactoryImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.DynamicAny.DynAnyFactory"), "interface org.omg.DynamicAny.DynAnyFactory");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("_ids"), "method _ids");
        check(mn.contains("create_dyn_any"), "method create_dyn_any");
        check(mn.contains("create_dyn_any_from_type_code"), "method create_dyn_any_from_type_code");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
