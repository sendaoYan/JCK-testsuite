/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.Utils conformance: type shape via reflection (headless-safe).
 * @run main UtilsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Utils", false, UtilsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "parseIdFromSameDocumentURI"), "declares parseIdFromSameDocumentURI");
        check(hasMethod(c, "readBytesFromStream"), "declares readBytesFromStream");
        check(hasMethod(c, "sameDocumentURI"), "declares sameDocumentURI");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Utils");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("parseIdFromSameDocumentURI", "readBytesFromStream", "sameDocumentURI"))),
                "exact public/protected method face (3 names): " + names);
        check(staticNames.equals(new java.util.HashSet<String>(java.util.Arrays.asList("parseIdFromSameDocumentURI", "readBytesFromStream", "sameDocumentURI"))), "static member names locked");
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 0, "exactly 0 declared constructors, got " + ctorFace.size());
        // black-box behavior
        byte[] data = new byte[]{1, 2, 3};
        byte[] read = org.jcp.xml.dsig.internal.dom.Utils.readBytesFromStream(
                new java.io.ByteArrayInputStream(data));
        check(java.util.Arrays.equals(data, read), "readBytesFromStream round trip");
        byte[] empty = org.jcp.xml.dsig.internal.dom.Utils.readBytesFromStream(
                new java.io.ByteArrayInputStream(new byte[0]));
        check(empty != null && empty.length == 0, "empty stream yields empty bytes");
        check("abc".equals(org.jcp.xml.dsig.internal.dom.Utils.parseIdFromSameDocumentURI("#abc")),
                "fragment URI yields the bare id");
        check(org.jcp.xml.dsig.internal.dom.Utils.parseIdFromSameDocumentURI("") == null,
                "empty URI yields null");
        check(org.jcp.xml.dsig.internal.dom.Utils.sameDocumentURI("#x"), "fragment URI is same-document");
        check(!org.jcp.xml.dsig.internal.dom.Utils.sameDocumentURI("http://x/#a"),
                "absolute URI is not same-document");
    }
}
