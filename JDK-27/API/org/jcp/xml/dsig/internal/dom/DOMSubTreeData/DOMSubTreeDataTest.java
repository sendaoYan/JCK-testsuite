/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMSubTreeData conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMSubTreeDataTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMSubTreeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSubTreeData", false, DOMSubTreeDataTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMSubTreeData"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.NodeSetData"), "iface:javax.xml.crypto.NodeSetData");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("excludeComments"), "method:excludeComments");
        check(methods.contains("getRoot"), "method:getRoot");
        check(methods.contains("iterator"), "method:iterator");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMSubTreeData");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.NodeSetData"))), "exact interface set");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("excludeComments", "getRoot", "iterator"))),
                "exact public/protected method face (3 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("org.w3c.dom.Node,boolean,"), "constructor(Node, boolean) present");
        // black-box behavior
        javax.xml.parsers.DocumentBuilder b = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = b.newDocument();
        org.w3c.dom.Element e = d.createElement("root");
        d.appendChild(e);
        org.jcp.xml.dsig.internal.dom.DOMSubTreeData st = new org.jcp.xml.dsig.internal.dom.DOMSubTreeData(e, true);
        check(st.getRoot() == e, "getRoot forwards the supplied node");
        check(st.excludeComments(), "excludeComments true when requested");
        org.jcp.xml.dsig.internal.dom.DOMSubTreeData st2 = new org.jcp.xml.dsig.internal.dom.DOMSubTreeData(e, false);
        check(!st2.excludeComments(), "excludeComments false when requested");
        check(st2.getRoot() == e, "both flag values keep the same root");
        check(st instanceof javax.xml.crypto.NodeSetData, "DOMSubTreeData is a NodeSetData");
    }
}
