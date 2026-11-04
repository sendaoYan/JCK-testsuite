/*
 * @test
 * @summary com.sun.corba.se.impl.io.ObjectStreamClass conformance: type shape via reflection (headless-safe).
 * @run main ObjectStreamClassTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectStreamClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.ObjectStreamClass", false, ObjectStreamClassTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "interface java.io.Serializable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("forClass"), "method forClass");
        check(mn.contains("getActualSerialVersionUID"), "method getActualSerialVersionUID");
        check(mn.contains("getActualSerialVersionUIDStr"), "method getActualSerialVersionUIDStr");
        check(mn.contains("getField"), "method getField");
        check(mn.contains("getFields"), "method getFields");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getRMIIIOPOptionalDataRepId"), "method getRMIIIOPOptionalDataRepId");
        check(mn.contains("getSerialVersionUID"), "method getSerialVersionUID");
        check(mn.contains("getSerialVersionUIDStr"), "method getSerialVersionUIDStr");
        check(mn.contains("hasField"), "method hasField");
        check(mn.contains("readResolve"), "method readResolve");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("writeReplace"), "method writeReplace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
