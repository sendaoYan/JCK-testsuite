/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMKeyName conformance: type shape via reflection (headless-safe).
 * @run main DOMKeyNameTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMKeyNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyName", false, DOMKeyNameTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.keyinfo.KeyName"), "implements javax.xml.crypto.dsig.keyinfo.KeyName");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "marshal"), "declares marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyName");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.keyinfo.KeyName"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("equals", "getName", "hashCode", "marshal"))),
                "exact public/protected method face (4 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 2, "exactly 2 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("java.lang.String,"), "constructor(String) present");
        check(ctorFace.contains("org.w3c.dom.Element,"), "constructor(Element) present");
        // black-box behavior
        org.jcp.xml.dsig.internal.dom.DOMKeyName kn = new org.jcp.xml.dsig.internal.dom.DOMKeyName("mykey");
        check("mykey".equals(kn.getName()), "DOMKeyName(String) getName round trip");
        check(kn.equals(new org.jcp.xml.dsig.internal.dom.DOMKeyName("mykey")), "equal names are equal");
        check(!kn.equals(new org.jcp.xml.dsig.internal.dom.DOMKeyName("other")), "different names are unequal");
        check(kn.hashCode() == new org.jcp.xml.dsig.internal.dom.DOMKeyName("mykey").hashCode(), "equal names share hashCode");
        check(!kn.isFeatureSupported("dom"), "unknown feature reported unsupported");
        try {
            kn.isFeatureSupported(null);
            check(false, "expected NPE for isFeatureSupported(null)");
        } catch (NullPointerException expected) {
            check(true, "isFeatureSupported(null) -> NPE");
        }
        check(kn instanceof javax.xml.crypto.dsig.keyinfo.KeyName, "DOMKeyName is a KeyName");
        check(kn instanceof javax.xml.crypto.XMLStructure, "DOMKeyName is an XMLStructure");
        check(!kn.equals("mykey"), "not equal to a String");
    }
}
