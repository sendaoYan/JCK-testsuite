/*
 * @test
 * @summary com.sun.corba.se.spi.ior.ObjectKeyTemplate conformance: type shape via reflection (headless-safe).
 * @run main ObjectKeyTemplateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectKeyTemplateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.ObjectKeyTemplate", false, ObjectKeyTemplateTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.Writeable"), "interface com.sun.corba.se.spi.ior.Writeable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("getAdapterId"), "method getAdapterId");
        check(mn.contains("getORBId"), "method getORBId");
        check(mn.contains("getORBVersion"), "method getORBVersion");
        check(mn.contains("getObjectAdapterId"), "method getObjectAdapterId");
        check(mn.contains("getServerId"), "method getServerId");
        check(mn.contains("getServerRequestDispatcher"), "method getServerRequestDispatcher");
        check(mn.contains("getSubcontractId"), "method getSubcontractId");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
