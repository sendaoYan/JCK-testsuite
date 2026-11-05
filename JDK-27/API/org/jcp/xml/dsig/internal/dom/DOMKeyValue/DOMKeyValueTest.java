/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMKeyValue conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMKeyValueTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMKeyValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyValue", false, DOMKeyValueTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMKeyValue"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.keyinfo.KeyValue"), "iface:javax.xml.crypto.dsig.keyinfo.KeyValue");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getPublicKey"), "method:getPublicKey");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyValue");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.keyinfo.KeyValue"))), "exact interface set");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public abstract]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("decode", "equals", "getPublicKey", "hashCode", "marshal"))),
                "exact public/protected method face (5 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("decode"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.security.PublicKey,"), "constructor(PublicKey) present");
        check(ctorFace.contains("org.w3c.dom.Element,"), "constructor(Element) present");
        // black-box behavior
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        java.security.PublicKey pk = kpg.generateKeyPair().getPublic();
        javax.xml.crypto.dsig.keyinfo.KeyValue kv =
                javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance("DOM").newKeyValue(pk);
        check(kv instanceof org.jcp.xml.dsig.internal.dom.DOMKeyValue,
                "factory newKeyValue wiring is the DOMKeyValue family");
        check(kv.getPublicKey() == pk, "public key forwarded unchanged");
        java.security.KeyPairGenerator dkg = java.security.KeyPairGenerator.getInstance("DSA");
        dkg.initialize(1024);
        javax.xml.crypto.dsig.keyinfo.KeyValue kv2 =
                javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance("DOM").newKeyValue(dkg.generateKeyPair().getPublic());
        check(kv2 instanceof org.jcp.xml.dsig.internal.dom.DOMKeyValue, "DSA keys wired to the same family");
    }
}
