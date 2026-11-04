/*
 * @test
 * @summary com.sun.corba.se.impl.orb.ParserTable.TestBadServerIdHandler conformance: type shape via reflection (headless-safe).
 * @run main TestBadServerIdHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TestBadServerIdHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orb.ParserTable$TestBadServerIdHandler", false, TestBadServerIdHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.oa.poa.BadServerIdHandler"), "interface com.sun.corba.se.impl.oa.poa.BadServerIdHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("handle"), "method handle");
        check(mn.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
