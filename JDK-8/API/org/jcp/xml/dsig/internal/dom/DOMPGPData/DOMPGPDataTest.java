/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMPGPData conformance: type shape via reflection (headless-safe).
 * @run main DOMPGPDataTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMPGPDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMPGPData", false, DOMPGPDataTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.jcp.xml.dsig.internal.dom.DOMStructure"), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        check(hasInterface(c, "javax.xml.crypto.dsig.keyinfo.PGPData"), "implements javax.xml.crypto.dsig.keyinfo.PGPData");
        check(hasMethod(c, "getExternalElements"), "declares getExternalElements");
        check(hasMethod(c, "getKeyId"), "declares getKeyId");
        check(hasMethod(c, "getKeyPacket"), "declares getKeyPacket");
        check(hasMethod(c, "marshal"), "declares marshal");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMPGPData");
        check("org.jcp.xml.dsig.internal.dom.DOMStructure".equals(c.getSuperclass().getName()), "superclass org.jcp.xml.dsig.internal.dom.DOMStructure");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList("javax.xml.crypto.dsig.keyinfo.PGPData"))), "exact interface set");
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
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getExternalElements", "getKeyId", "getKeyPacket", "marshal"))),
                "exact public/protected method face (4 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 3, "exactly 3 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains("[B,java.util.List,"), "constructor(byte[], List) present");
        check(ctorFace.contains("[B,[B,java.util.List,"), "constructor(byte[], byte[], List) present");
        check(ctorFace.contains("org.w3c.dom.Element,"), "constructor(Element) present");
        // black-box behavior
        try {
            new org.jcp.xml.dsig.internal.dom.DOMPGPData(new byte[]{1, 2, 3}, null);
            check(false, "expected IAE for bad packet tag");
        } catch (IllegalArgumentException expected) {
            check("keypacket tag is invalid: bit 7 is not set".equals(expected.getMessage()),
                    "bad tag IAE message");
        }
        try {
            new org.jcp.xml.dsig.internal.dom.DOMPGPData(new byte[0], null);
            check(false, "expected IAE for short packet");
        } catch (IllegalArgumentException expected) {
            check("keypacket must be at least 3 bytes long".equals(expected.getMessage()),
                    "short packet IAE message");
        }
        org.jcp.xml.dsig.internal.dom.DOMPGPData pd =
                new org.jcp.xml.dsig.internal.dom.DOMPGPData(new byte[]{(byte) 0xC5, 1, 2, 3}, null);
        check(pd.getKeyId() == null, "no key id in the one-array constructor");
        check(pd.getKeyPacket() != null && pd.getKeyPacket().length == 4, "key packet round trip");
        check(pd.getExternalElements().isEmpty(), "no external elements by default");
        try {
            new org.jcp.xml.dsig.internal.dom.DOMPGPData(new byte[]{1, 2},
                    new byte[]{(byte) 0xC6, 2, 3, 4}, null);
            check(false, "expected IAE for short key id");
        } catch (IllegalArgumentException expected) {
            check("keyId must be 8 bytes long".equals(expected.getMessage()), "short key id IAE message");
        }
        org.jcp.xml.dsig.internal.dom.DOMPGPData pd2 =
                new org.jcp.xml.dsig.internal.dom.DOMPGPData(new byte[]{1, 2, 3, 4, 5, 6, 7, 8},
                        new byte[]{(byte) 0xC6, 2, 3, 4}, null);
        check(pd2.getKeyId() != null && pd2.getKeyId().length == 8, "key id round trip");
        check(pd2.getKeyPacket() != null && pd2.getKeyPacket().length == 4, "packet round trip");
        check(pd instanceof javax.xml.crypto.dsig.keyinfo.PGPData, "DOMPGPData is a PGPData");
    }
}
