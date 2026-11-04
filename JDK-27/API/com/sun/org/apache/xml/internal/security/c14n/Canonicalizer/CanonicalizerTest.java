/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.Canonicalizer conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n:+open
 * @run main CanonicalizerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CanonicalizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.Canonicalizer", false, CanonicalizerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.c14n.Canonicalizer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("canonicalizeSubtree"), "method:canonicalizeSubtree");
        check(methods.contains("canonicalizeXPathNodeSet"), "method:canonicalizeXPathNodeSet");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("register"), "method:register");
        check(methods.contains("registerDefaultAlgorithms"), "method:registerDefaultAlgorithms");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALGO_ID_C14N11_OMIT_COMMENTS"), "field:ALGO_ID_C14N11_OMIT_COMMENTS");
        check(fields.contains("ALGO_ID_C14N11_WITH_COMMENTS"), "field:ALGO_ID_C14N11_WITH_COMMENTS");
        check(fields.contains("ALGO_ID_C14N_EXCL_OMIT_COMMENTS"), "field:ALGO_ID_C14N_EXCL_OMIT_COMMENTS");
        check(fields.contains("ALGO_ID_C14N_EXCL_WITH_COMMENTS"), "field:ALGO_ID_C14N_EXCL_WITH_COMMENTS");
        check(fields.contains("ALGO_ID_C14N_OMIT_COMMENTS"), "field:ALGO_ID_C14N_OMIT_COMMENTS");
        check(fields.contains("ALGO_ID_C14N_PHYSICAL"), "field:ALGO_ID_C14N_PHYSICAL");
        check(fields.contains("ALGO_ID_C14N_WITH_COMMENTS"), "field:ALGO_ID_C14N_WITH_COMMENTS");
        check(fields.contains("ENCODING"), "field:ENCODING");
        check(fields.contains("XPATH_C14N_WITH_COMMENTS_SINGLE_NODE"), "field:XPATH_C14N_WITH_COMMENTS_SINGLE_NODE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
