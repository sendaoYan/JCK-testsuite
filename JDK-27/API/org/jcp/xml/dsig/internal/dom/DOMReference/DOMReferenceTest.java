/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMReference conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMReferenceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMReference", false, DOMReferenceTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMReference"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dom.DOMURIReference"), "iface:javax.xml.crypto.dom.DOMURIReference");
        check(ifaces.contains("javax.xml.crypto.dsig.Reference"), "iface:javax.xml.crypto.dsig.Reference");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("digest"), "method:digest");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getCalculatedDigestValue"), "method:getCalculatedDigestValue");
        check(methods.contains("getDereferencedData"), "method:getDereferencedData");
        check(methods.contains("getDigestInputStream"), "method:getDigestInputStream");
        check(methods.contains("getDigestMethod"), "method:getDigestMethod");
        check(methods.contains("getDigestValue"), "method:getDigestValue");
        check(methods.contains("getHere"), "method:getHere");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getTransforms"), "method:getTransforms");
        check(methods.contains("getType"), "method:getType");
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        check(methods.contains("validate"), "method:validate");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAXIMUM_TRANSFORM_COUNT"), "field:MAXIMUM_TRANSFORM_COUNT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
