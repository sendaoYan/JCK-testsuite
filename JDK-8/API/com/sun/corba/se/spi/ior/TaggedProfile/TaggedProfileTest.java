/*
 * @test
 * @summary com.sun.corba.se.spi.ior.TaggedProfile conformance: type shape via reflection (headless-safe).
 * @run main TaggedProfileTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TaggedProfileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.TaggedProfile", false, TaggedProfileTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.Identifiable"), "interface com.sun.corba.se.spi.ior.Identifiable");
        check(in.contains("com.sun.corba.se.spi.ior.MakeImmutable"), "interface com.sun.corba.se.spi.ior.MakeImmutable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("getIOPProfile"), "method getIOPProfile");
        check(mn.contains("getObjectId"), "method getObjectId");
        check(mn.contains("getObjectKey"), "method getObjectKey");
        check(mn.contains("getObjectKeyTemplate"), "method getObjectKeyTemplate");
        check(mn.contains("getTaggedProfileTemplate"), "method getTaggedProfileTemplate");
        check(mn.contains("isEquivalent"), "method isEquivalent");
        check(mn.contains("isLocal"), "method isLocal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
