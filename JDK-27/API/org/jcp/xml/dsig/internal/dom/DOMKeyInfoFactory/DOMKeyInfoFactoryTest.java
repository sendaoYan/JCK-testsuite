/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMKeyInfoFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMKeyInfoFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory", false, DOMKeyInfoFactoryTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dsig.keyinfo.KeyInfoFactory"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getURIDereferencer"), "method:getURIDereferencer");
        check(methods.contains("isFeatureSupported"), "method:isFeatureSupported");
        check(methods.contains("newKeyInfo"), "method:newKeyInfo");
        check(methods.contains("newKeyName"), "method:newKeyName");
        check(methods.contains("newKeyValue"), "method:newKeyValue");
        check(methods.contains("newPGPData"), "method:newPGPData");
        check(methods.contains("newRetrievalMethod"), "method:newRetrievalMethod");
        check(methods.contains("newX509Data"), "method:newX509Data");
        check(methods.contains("newX509IssuerSerial"), "method:newX509IssuerSerial");
        check(methods.contains("unmarshalKeyInfo"), "method:unmarshalKeyInfo");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory");
        check("javax.xml.crypto.dsig.keyinfo.KeyInfoFactory".equals(c.getSuperclass().getName()), "superclass javax.xml.crypto.dsig.keyinfo.KeyInfoFactory");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getURIDereferencer", "isFeatureSupported", "newKeyInfo", "newKeyName", "newKeyValue", "newPGPData", "newRetrievalMethod", "newX509Data", "newX509IssuerSerial", "unmarshalKeyInfo"))),
                "exact public/protected method face (10 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains(""), "constructor(no-arg) present");
        // black-box behavior
        javax.xml.crypto.dsig.keyinfo.KeyInfoFactory kif =
                javax.xml.crypto.dsig.keyinfo.KeyInfoFactory.getInstance("DOM");
        check(kif instanceof org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory,
                "getInstance(DOM) returns the DOM implementation");
        check("DOM".equals(kif.getMechanismType()), "mechanism type DOM");
        check("XMLDSig".equals(kif.getProvider().getName()), "provider XMLDSig");
        org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory direct =
                new org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory();
        check(direct.getMechanismType() == null, "direct construction leaves the mechanism type unset (registration-driven)");
        javax.xml.crypto.dsig.keyinfo.KeyName kn = direct.newKeyName("abc");
        check("abc".equals(kn.getName()), "factory produces usable KeyNames");
        javax.xml.crypto.dsig.keyinfo.KeyInfo ki = direct.newKeyInfo(java.util.Arrays.asList(kn), "i1");
        check("i1".equals(ki.getId()), "factory produces usable KeyInfos");
        check(!kif.isFeatureSupported("dom"), "unknown feature unsupported");
        try {
            kif.isFeatureSupported(null);
            check(false, "expected NPE for isFeatureSupported(null)");
        } catch (NullPointerException expected) {
            check(true, "isFeatureSupported(null) -> NPE");
        }
    }
}
