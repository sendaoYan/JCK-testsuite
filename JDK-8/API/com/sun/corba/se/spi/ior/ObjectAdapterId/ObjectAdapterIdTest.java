/*
 * @test
 * @summary com.sun.corba.se.spi.ior.ObjectAdapterId conformance: type shape via reflection (headless-safe).
 * @run main ObjectAdapterIdTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectAdapterIdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.ObjectAdapterId", false, ObjectAdapterIdTest.class.getClassLoader());
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
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("getAdapterName"), "method getAdapterName");
        check(mn.contains("getNumLevels"), "method getNumLevels");
        check(mn.contains("iterator"), "method iterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
