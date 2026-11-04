/*
 * @test
 * @summary com.sun.corba.se.impl.ior.iiop.CodeSetsComponentImpl conformance: type shape via reflection (headless-safe).
 * @run main CodeSetsComponentImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CodeSetsComponentImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.iiop.CodeSetsComponentImpl", false, CodeSetsComponentImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.ior.TaggedComponentBase"), "superclass com.sun.corba.se.spi.ior.TaggedComponentBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.iiop.CodeSetsComponent"), "interface com.sun.corba.se.spi.ior.iiop.CodeSetsComponent");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getCodeSetComponentInfo"), "method getCodeSetComponentInfo");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("writeContents"), "method writeContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
