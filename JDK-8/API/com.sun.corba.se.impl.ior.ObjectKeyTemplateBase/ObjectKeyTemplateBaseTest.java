/*
 * @test
 * @summary com.sun.corba.se.impl.ior.ObjectKeyTemplateBase conformance: type shape via reflection (headless-safe).
 * @run main ObjectKeyTemplateBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectKeyTemplateBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.ObjectKeyTemplateBase", false, ObjectKeyTemplateBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.ObjectKeyTemplate"), "interface com.sun.corba.se.spi.ior.ObjectKeyTemplate");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getAdapterId"), "method getAdapterId");
        check(mn.contains("getORBId"), "method getORBId");
        check(mn.contains("getORBVersion"), "method getORBVersion");
        check(mn.contains("getObjectAdapterId"), "method getObjectAdapterId");
        check(mn.contains("getServerId"), "method getServerId");
        check(mn.contains("getServerRequestDispatcher"), "method getServerRequestDispatcher");
        check(mn.contains("getSubcontractId"), "method getSubcontractId");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("setORBVersion"), "method setORBVersion");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
