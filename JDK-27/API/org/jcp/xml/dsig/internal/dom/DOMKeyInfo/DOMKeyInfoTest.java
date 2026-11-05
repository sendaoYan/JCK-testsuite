/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMKeyInfo conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMKeyInfoTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMKeyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyInfo", false, DOMKeyInfoTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMKeyInfo"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.dsig.keyinfo.KeyInfo"), "iface:javax.xml.crypto.dsig.keyinfo.KeyInfo");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getContent"), "method:getContent");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("marshal"), "method:marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyInfo");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.keyinfo.KeyInfo"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getContent", "getId", "hashCode", "marshal"))),
                "exact public/protected method face (5 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getContent"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("org.w3c.dom.Element,javax.xml.crypto.XMLCryptoContext,java.security.Provider,"), "constructor(Element, XMLCryptoContext, Provider) present");
        check(ctorFace.contains("java.util.List,java.lang.String,"), "constructor(List, String) present");
        // black-box behavior
        try {
            new org.jcp.xml.dsig.internal.dom.DOMKeyInfo(java.util.Collections.<javax.xml.crypto.XMLStructure>emptyList(), "ki1");
            check(false, "expected IAE for empty content");
        } catch (IllegalArgumentException expected) {
            check("content cannot be empty".equals(expected.getMessage()), "empty content IAE message");
        }
        org.jcp.xml.dsig.internal.dom.DOMKeyName kn = new org.jcp.xml.dsig.internal.dom.DOMKeyName("k2");
        org.jcp.xml.dsig.internal.dom.DOMKeyInfo ki =
                new org.jcp.xml.dsig.internal.dom.DOMKeyInfo(java.util.Arrays.<javax.xml.crypto.XMLStructure>asList(kn), "ki1");
        check("ki1".equals(ki.getId()), "getId round trip");
        check(ki.getContent().size() == 1 && ki.getContent().get(0) == kn, "content forwarded unchanged");
        try {
            ki.getContent().add((javax.xml.crypto.XMLStructure) null);
            check(false, "expected UOE modifying content");
        } catch (UnsupportedOperationException expected) {
            check(true, "content list is unmodifiable");
        }
        org.jcp.xml.dsig.internal.dom.DOMKeyInfo ki2 =
                new org.jcp.xml.dsig.internal.dom.DOMKeyInfo(java.util.Arrays.<javax.xml.crypto.XMLStructure>asList(kn), null);
        check(ki2.getId() == null, "null id stays null");
        check(ki instanceof javax.xml.crypto.dsig.keyinfo.KeyInfo, "DOMKeyInfo is a KeyInfo");
    }
}
