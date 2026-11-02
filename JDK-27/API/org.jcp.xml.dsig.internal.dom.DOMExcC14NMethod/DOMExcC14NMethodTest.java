/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMExcC14NMethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMExcC14NMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod", false, DOMExcC14NMethodTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMExcC14NMethod"), "name");
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
        check(methods.contains("getParameterSpecPrefixList"), "method:getParameterSpecPrefixList");
        check(methods.contains("getParamsNSURI"), "method:getParamsNSURI");
        check(methods.contains("init"), "method:init");
        check(methods.contains("marshalParams"), "method:marshalParams");
        check(methods.contains("transform"), "method:transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
