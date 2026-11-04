/*
 * @test
 * @summary com.sun.corba.se.impl.ior.GenericTaggedProfile conformance: type shape via reflection (headless-safe).
 * @run main GenericTaggedProfileTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class GenericTaggedProfileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.GenericTaggedProfile", false, GenericTaggedProfileTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.ior.GenericIdentifiable"), "superclass com.sun.corba.se.impl.ior.GenericIdentifiable");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.TaggedProfile"), "interface com.sun.corba.se.spi.ior.TaggedProfile");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("getIOPProfile"), "method getIOPProfile");
        check(mn.contains("getObjectId"), "method getObjectId");
        check(mn.contains("getObjectKey"), "method getObjectKey");
        check(mn.contains("getObjectKeyTemplate"), "method getObjectKeyTemplate");
        check(mn.contains("getTaggedProfileTemplate"), "method getTaggedProfileTemplate");
        check(mn.contains("isEquivalent"), "method isEquivalent");
        check(mn.contains("isLocal"), "method isLocal");
        check(mn.contains("makeImmutable"), "method makeImmutable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
