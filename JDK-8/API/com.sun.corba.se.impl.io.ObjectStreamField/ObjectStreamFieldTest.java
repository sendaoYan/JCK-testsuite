/*
 * @test
 * @summary com.sun.corba.se.impl.io.ObjectStreamField conformance: type shape via reflection (headless-safe).
 * @run main ObjectStreamFieldTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectStreamFieldTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.ObjectStreamField", false, ObjectStreamFieldTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.lang.Comparable"), "interface java.lang.Comparable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("compareTo"), "method compareTo");
        check(mn.contains("getClazz"), "method getClazz");
        check(mn.contains("getFieldID"), "method getFieldID");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getSignature"), "method getSignature");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getTypeCode"), "method getTypeCode");
        check(mn.contains("getTypeString"), "method getTypeString");
        check(mn.contains("isPrimitive"), "method isPrimitive");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("typeEquals"), "method typeEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
