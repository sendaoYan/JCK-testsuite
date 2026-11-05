/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheNodeSetData conformance: type shape via reflection (headless-safe).
 * @run main ApacheNodeSetDataTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ApacheNodeSetDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheNodeSetData", false, ApacheNodeSetDataTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "org.jcp.xml.dsig.internal.dom.ApacheData"), "implements org.jcp.xml.dsig.internal.dom.ApacheData");
        check(hasInterface(c, "javax.xml.crypto.NodeSetData"), "implements javax.xml.crypto.NodeSetData");
        check(hasMethod(c, "getXMLSignatureInput"), "declares getXMLSignatureInput");
        check(hasMethod(c, "iterator"), "declares iterator");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheNodeSetData");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("org.jcp.xml.dsig.internal.dom.ApacheData", "javax.xml.crypto.NodeSetData"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getXMLSignatureInput", "iterator"))),
                "exact public/protected method face (2 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput,"), "constructor(XMLSignatureInput) present");
        // black-box behavior
        Class<?> xsi = Class.forName("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput");
        javax.xml.parsers.DocumentBuilder b = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = b.newDocument();
        org.w3c.dom.Element e = d.createElement("root");
        d.appendChild(e);
        Object nodeInput = xsi.getConstructor(org.w3c.dom.Node.class).newInstance(e);
        Object ansd = c.getConstructor(xsi).newInstance(nodeInput);
        check(ansd instanceof javax.xml.crypto.NodeSetData, "ApacheNodeSetData is a NodeSetData");
        check(ansd instanceof org.jcp.xml.dsig.internal.dom.ApacheData, "ApacheNodeSetData implements ApacheData");
        check(c.getMethod("getXMLSignatureInput").invoke(ansd) == nodeInput,
                "getXMLSignatureInput returns the wrapped input");
        int n = 0;
        java.util.Iterator<?> nodeIter = ((org.jcp.xml.dsig.internal.dom.ApacheNodeSetData) ansd).iterator();
        while (nodeIter.hasNext()) { nodeIter.next(); n++; }
        check(n == 1, "node-set iterator yields the supplied node");
        Object byteInput = xsi.getConstructor(byte[].class).newInstance((Object) new byte[]{9});
        Object byteBacked = c.getConstructor(xsi).newInstance(byteInput);
        try {
            ((org.jcp.xml.dsig.internal.dom.ApacheNodeSetData) byteBacked).iterator().next();
            check(false, "expected failure iterating a byte-backed node-set");
        } catch (RuntimeException expected) {
            check(true, "byte-backed node-set cannot be iterated as nodes");
        }
    }
}
