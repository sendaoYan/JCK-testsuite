/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory conformance: type shape via reflection (headless-safe).
 * @run main DOMKeyInfoFactoryTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMKeyInfoFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory", false, DOMKeyInfoFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.crypto.dsig.keyinfo.KeyInfoFactory"), "superclass javax.xml.crypto.dsig.keyinfo.KeyInfoFactory");
        check(hasMethod(c, "getURIDereferencer"), "declares getURIDereferencer");
        check(hasMethod(c, "isFeatureSupported"), "declares isFeatureSupported");
        check(hasMethod(c, "newKeyInfo"), "declares newKeyInfo");
        check(hasMethod(c, "newKeyName"), "declares newKeyName");
        check(hasMethod(c, "newKeyValue"), "declares newKeyValue");
        check(hasMethod(c, "newPGPData"), "declares newPGPData");
        check(hasMethod(c, "newRetrievalMethod"), "declares newRetrievalMethod");
        check(hasMethod(c, "newX509Data"), "declares newX509Data");
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
