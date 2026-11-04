/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315Excl conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n.implementations:+open
 * @run main Canonicalizer20010315ExclTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Canonicalizer20010315ExclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315Excl", false, Canonicalizer20010315ExclTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315Excl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("circumventBugIfNeeded"), "method:circumventBugIfNeeded");
        check(methods.contains("engineCanonicalize"), "method:engineCanonicalize");
        check(methods.contains("engineCanonicalizeSubTree"), "method:engineCanonicalizeSubTree");
        check(methods.contains("engineCanonicalizeXPathNodeSet"), "method:engineCanonicalizeXPathNodeSet");
        check(methods.contains("outputAttributes"), "method:outputAttributes");
        check(methods.contains("outputAttributesSubtree"), "method:outputAttributesSubtree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
