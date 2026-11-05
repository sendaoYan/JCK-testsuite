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
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.keyinfo.RetrievalMethod", "javax.xml.crypto.dom.DOMURIReference"))), "exact interface set");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "modifier set [public final]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("dereference", "dereferenceAsXMLStructure", "equals", "getHere", "getTransforms", "getType", "getURI", "hashCode", "marshal"))),
                "exact public/protected method face (9 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.lang.String,java.lang.String,java.util.List,"), "constructor(String, String, List) present");
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        // black-box behavior
        try {
            new org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod(null, "type", null);
            check(false, "expected NPE for null URI");
        } catch (NullPointerException expected) {
            check("uri cannot be null".equals(expected.getMessage()), "null URI NPE message");
        }
        org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod rm =
                new org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod("#loc", "typeX",
                        java.util.Collections.<javax.xml.crypto.dsig.Transform>emptyList());
        check("#loc".equals(rm.getURI()), "getURI round trip");
        check("typeX".equals(rm.getType()), "getType round trip");
        check(rm.getTransforms().isEmpty(), "empty transform list retained");
        org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod rm2 =
                new org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod("#other", null, null);
        check("#other".equals(rm2.getURI()) && rm2.getType() == null, "null type stays null");
        check(rm instanceof javax.xml.crypto.dsig.keyinfo.RetrievalMethod, "DOMRetrievalMethod is a RetrievalMethod");
    }
}
