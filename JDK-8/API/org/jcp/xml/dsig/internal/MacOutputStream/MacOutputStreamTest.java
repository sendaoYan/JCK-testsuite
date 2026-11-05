/*
 * @test
 * @summary org.jcp.xml.dsig.internal.MacOutputStream conformance: type shape via reflection (headless-safe).
 * @run main MacOutputStreamTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MacOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.MacOutputStream", false, MacOutputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.ByteArrayOutputStream"), "superclass java.io.ByteArrayOutputStream");
        check(hasMethod(c, "write"), "declares write");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.MacOutputStream");
        check("java.io.ByteArrayOutputStream".equals(c.getSuperclass().getName()), "superclass java.io.ByteArrayOutputStream");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("write"))),
                "exact public/protected method face (1 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("javax.crypto.Mac,"), "constructor(Mac) present");
        // black-box behavior
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256"));
        org.jcp.xml.dsig.internal.MacOutputStream mos = new org.jcp.xml.dsig.internal.MacOutputStream(mac);
        check(mos instanceof java.io.ByteArrayOutputStream, "MacOutputStream buffers via ByteArrayOutputStream");
        mos.write(new byte[]{4, 5, 6});
        check(mos.size() == 3, "written bytes are buffered");
        byte[] tag = mac.doFinal();
        javax.crypto.Mac mac2 = javax.crypto.Mac.getInstance("HmacSHA256");
        mac2.init(new javax.crypto.spec.SecretKeySpec(new byte[32], "HmacSHA256"));
        mac2.update(new byte[]{4, 5, 6});
        check(java.util.Arrays.equals(tag, mac2.doFinal()), "mac.doFinal matches an independent MAC over the same bytes");
        org.jcp.xml.dsig.internal.MacOutputStream mos2 =
                new org.jcp.xml.dsig.internal.MacOutputStream(javax.crypto.Mac.getInstance("HmacSHA256"));
        check(mos2 != null, "second instance with a fresh mac constructs");
    }
}
