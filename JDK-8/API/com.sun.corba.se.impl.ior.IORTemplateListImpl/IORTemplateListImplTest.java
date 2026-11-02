/*
 * @test
 * @summary com.sun.corba.se.impl.ior.IORTemplateListImpl conformance: type shape via reflection (headless-safe).
 * @run main IORTemplateListImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IORTemplateListImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.IORTemplateListImpl", false, IORTemplateListImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.ior.FreezableList"), "superclass com.sun.corba.se.impl.ior.FreezableList");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.IORTemplateList"), "interface com.sun.corba.se.spi.ior.IORTemplateList");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("add"), "method add");
        check(mn.contains("isEquivalent"), "method isEquivalent");
        check(mn.contains("makeIOR"), "method makeIOR");
        check(mn.contains("makeImmutable"), "method makeImmutable");
        check(mn.contains("set"), "method set");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
