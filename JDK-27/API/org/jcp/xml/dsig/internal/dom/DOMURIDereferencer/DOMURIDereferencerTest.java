/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMURIDereferencer conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMURIDereferencerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMURIDereferencerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMURIDereferencer", false, DOMURIDereferencerTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMURIDereferencer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.URIDereferencer"), "iface:javax.xml.crypto.URIDereferencer");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("dereference"), "method:dereference");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMURIDereferencer");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.URIDereferencer"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("dereference"))),
                "exact public/protected method face (1 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 0, "exactly 0 declared constructors, got " + ctorFace.size());
        // black-box behavior
        javax.xml.crypto.URIDereferencer ud =
                javax.xml.crypto.dsig.XMLSignatureFactory.getInstance("DOM").getURIDereferencer();
        check(ud instanceof org.jcp.xml.dsig.internal.dom.DOMURIDereferencer,
                "the factory wires the DOMURIDereferencer");
        try {
            ud.dereference(null, null);
            check(false, "expected NPE for dereference(null, null)");
        } catch (NullPointerException expected) {
            check("uriRef cannot be null".equals(expected.getMessage()), "null uriRef NPE message");
        }
    }
}
