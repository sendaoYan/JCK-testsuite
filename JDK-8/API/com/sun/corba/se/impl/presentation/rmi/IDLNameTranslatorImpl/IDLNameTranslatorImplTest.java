/*
 * @test
 * @summary com.sun.corba.se.impl.presentation.rmi.IDLNameTranslatorImpl conformance: type shape via reflection (headless-safe).
 * @run main IDLNameTranslatorImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IDLNameTranslatorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.presentation.rmi.IDLNameTranslatorImpl", false, IDLNameTranslatorImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator"), "interface com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("charToUnicodeRepresentation"), "method charToUnicodeRepresentation");
        check(mn.contains("get"), "method get");
        check(mn.contains("getExceptionId"), "method getExceptionId");
        check(mn.contains("getIDLName"), "method getIDLName");
        check(mn.contains("getInterfaces"), "method getInterfaces");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("getMethods"), "method getMethods");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
