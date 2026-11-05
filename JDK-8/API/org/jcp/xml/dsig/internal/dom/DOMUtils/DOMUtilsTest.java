/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMUtils conformance: type shape via reflection (headless-safe).
 * @run main DOMUtilsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMUtils", false, DOMUtilsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "appendChild"), "declares appendChild");
        check(hasMethod(c, "createElement"), "declares createElement");
        check(hasMethod(c, "getAttributeValue"), "declares getAttributeValue");
        check(hasMethod(c, "getFirstChildElement"), "declares getFirstChildElement");
        check(hasMethod(c, "getIdAttributeValue"), "declares getIdAttributeValue");
        check(hasMethod(c, "getLastChildElement"), "declares getLastChildElement");
        check(hasMethod(c, "getNSPrefix"), "declares getNSPrefix");
        check(hasMethod(c, "getNextSiblingElement"), "declares getNextSiblingElement");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMUtils");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("appendChild", "createElement", "getAttributeValue", "getFirstChildElement", "getIdAttributeValue", "getLastChildElement", "getNSPrefix", "getNextSiblingElement", "getOwnerDocument", "getQNameString", "getSignaturePrefix", "isNamespace", "nodeSet", "nodesEqual", "paramsEqual", "removeAllChildren", "setAttribute", "setAttributeID"))),
                "exact public/protected method face (18 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("appendChild", "createElement", "getAttributeValue", "getFirstChildElement", "getIdAttributeValue", "getLastChildElement", "getNSPrefix", "getNextSiblingElement", "getOwnerDocument", "getQNameString", "getSignaturePrefix", "isNamespace", "nodeSet", "nodesEqual", "paramsEqual", "removeAllChildren", "setAttribute", "setAttributeID"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 0, "exactly 0 declared constructors, got " + ctorFace.size());
        // black-box behavior
        javax.xml.parsers.DocumentBuilder b = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = b.newDocument();
        org.w3c.dom.Element e = d.createElement("test");
        d.appendChild(e);
        check(org.jcp.xml.dsig.internal.dom.DOMUtils.getOwnerDocument(e) == d, "getOwnerDocument");
        org.w3c.dom.Element created =
                org.jcp.xml.dsig.internal.dom.DOMUtils.createElement(d, "elem", "http://ns", "pre");
        check("pre".equals(created.getPrefix()) && "elem".equals(created.getLocalName())
                && "http://ns".equals(created.getNamespaceURI()), "createElement wires prefix/local/namespace");
        org.jcp.xml.dsig.internal.dom.DOMUtils.setAttribute(created, "k", "v");
        check("v".equals(org.jcp.xml.dsig.internal.dom.DOMUtils.getAttributeValue(created, "k")),
                "setAttribute/getAttributeValue round trip");
        org.jcp.xml.dsig.internal.dom.DOMUtils.setAttributeID(created, "id", "x1");
        check("x1".equals(created.getAttribute("id")) && created.getAttributeNode("id").isId(),
                "setAttributeID marks the attribute as an ID");
        check("x1".equals(org.jcp.xml.dsig.internal.dom.DOMUtils.getIdAttributeValue(created, "id")),
                "getIdAttributeValue round trip");
        check("p:local".equals(org.jcp.xml.dsig.internal.dom.DOMUtils.getQNameString("p", "local")),
                "getQNameString joins prefix and local name");
        check(org.jcp.xml.dsig.internal.dom.DOMUtils.getFirstChildElement(e) == null,
                "getFirstChildElement of an empty element is null");
        check(org.jcp.xml.dsig.internal.dom.DOMUtils.getLastChildElement(e) == null,
                "getLastChildElement of an empty element is null");
        check(org.jcp.xml.dsig.internal.dom.DOMUtils.getNextSiblingElement(e) == null,
                "getNextSiblingElement of the only element is null");
        check(org.jcp.xml.dsig.internal.dom.DOMUtils.nodesEqual(e, e), "nodesEqual on the same node");
        check(org.jcp.xml.dsig.internal.dom.DOMUtils.paramsEqual(
                (java.security.spec.AlgorithmParameterSpec) null, (java.security.spec.AlgorithmParameterSpec) null),
                "paramsEqual(null, null)");
        check(!org.jcp.xml.dsig.internal.dom.DOMUtils.paramsEqual(
                new javax.xml.crypto.dsig.spec.ExcC14NParameterSpec(java.util.Collections.<String>emptyList()), null),
                "paramsEqual(spec, null) is false");
        check(!org.jcp.xml.dsig.internal.dom.DOMUtils.isNamespace(d.createElement("x")),
                "a plain element is not a namespace node");
        org.jcp.xml.dsig.internal.dom.DOMUtils.appendChild(e, created);
        check(e.getFirstChild() == created, "appendChild wires the child");
        org.jcp.xml.dsig.internal.dom.DOMUtils.removeAllChildren(e);
        check(e.getFirstChild() == null, "removeAllChildren clears the children");
    }
}
