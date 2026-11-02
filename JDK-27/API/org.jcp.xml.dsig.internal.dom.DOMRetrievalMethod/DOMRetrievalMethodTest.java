/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMRetrievalMethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMRetrievalMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod", false, DOMRetrievalMethodTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dom.DOMURIReference"), "iface:javax.xml.crypto.dom.DOMURIReference");
        check(ifaces.contains("javax.xml.crypto.dsig.keyinfo.RetrievalMethod"), "iface:javax.xml.crypto.dsig.keyinfo.RetrievalMethod");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("dereference"), "method:dereference");
        check(methods.contains("dereferenceAsXMLStructure"), "method:dereferenceAsXMLStructure");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getHere"), "method:getHere");
        check(methods.contains("getTransforms"), "method:getTransforms");
        check(methods.contains("getType"), "method:getType");
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
