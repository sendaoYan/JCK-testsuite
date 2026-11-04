/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMCanonicalXMLC14N11MethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMCanonicalXMLC14N11MethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method", false, DOMCanonicalXMLC14N11MethodTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMCanonicalXMLC14N11Method"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("init"), "method:init");
        check(methods.contains("transform"), "method:transform");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("C14N_11"), "field:C14N_11");
        check(fields.contains("C14N_11_WITH_COMMENTS"), "field:C14N_11_WITH_COMMENTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
