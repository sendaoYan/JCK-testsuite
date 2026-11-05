/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLObject conformance: type shape via reflection (headless-safe).
 * @run main DOMXMLObjectTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMXMLObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLObject", false, DOMXMLObjectTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.XMLObject"), "implements javax.xml.crypto.dsig.XMLObject");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getContent"), "declares getContent");
        check(hasMethod(c, "getEncoding"), "declares getEncoding");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getMimeType"), "declares getMimeType");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "marshal"), "declares marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLObject");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.XMLObject"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getContent", "getEncoding", "getId", "getMimeType", "hashCode", "marshal"))),
                "exact public/protected method face (7 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.util.List,java.lang.String,java.lang.String,java.lang.String,"), "constructor(List, String, String, String) present");
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        // black-box behavior
        org.jcp.xml.dsig.internal.dom.DOMXMLObject xo =
                new org.jcp.xml.dsig.internal.dom.DOMXMLObject(null, "id1", "text/xml", "UTF-8");
        check("id1".equals(xo.getId()), "getId round trip");
        check("text/xml".equals(xo.getMimeType()), "getMimeType round trip");
        check("UTF-8".equals(xo.getEncoding()), "getEncoding round trip");
        check(xo.getContent().isEmpty(), "null content becomes an empty list");
        try {
            xo.getContent().add((javax.xml.crypto.XMLStructure) null);
            check(false, "expected UOE modifying content");
        } catch (UnsupportedOperationException expected) {
            check(true, "content list is unmodifiable");
        }
        org.jcp.xml.dsig.internal.dom.DOMXMLObject xo2 =
                new org.jcp.xml.dsig.internal.dom.DOMXMLObject(null, null, null, null);
        check(xo2.getId() == null && xo2.getMimeType() == null && xo2.getEncoding() == null,
                "all-null metadata stays null");
        check(xo instanceof javax.xml.crypto.dsig.XMLObject, "DOMXMLObject is an XMLObject");
    }
}
