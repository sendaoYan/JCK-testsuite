/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod conformance: type shape via reflection (headless-safe).
 * @run main DOMRetrievalMethodTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMRetrievalMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMRetrievalMethod", false, DOMRetrievalMethodTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.keyinfo.RetrievalMethod"), "implements javax.xml.crypto.dsig.keyinfo.RetrievalMethod");
        check(hasInterface(c, "javax.xml.crypto.dom.DOMURIReference"), "implements javax.xml.crypto.dom.DOMURIReference");
        check(hasMethod(c, "dereference"), "declares dereference");
        check(hasMethod(c, "dereferenceAsXMLStructure"), "declares dereferenceAsXMLStructure");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getHere"), "declares getHere");
        check(hasMethod(c, "getTransforms"), "declares getTransforms");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "getURI"), "declares getURI");
        check(hasMethod(c, "hashCode"), "declares hashCode");
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
