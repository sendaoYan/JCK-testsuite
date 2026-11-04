/*
 * @test
 * @summary com.sun.xml.internal.ws.db.glassfish.BridgeWrapper conformance: reflection (kind, superclass, interfaces, methods).
 * @run main BridgeWrapperTest
 */

import java.lang.reflect.Modifier;


public class BridgeWrapperTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.db.glassfish.BridgeWrapper", false, BridgeWrapperTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.db.glassfish.BridgeWrapper"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.spi.db.XMLBridge"), "implements com.sun.xml.internal.ws.spi.db.XMLBridge");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("context"), "method context");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getContext"), "method getContext");
        check(mn.contains("getTypeInfo"), "method getTypeInfo");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("marshal"), "method marshal");
        check(mn.contains("supportOutputStream"), "method supportOutputStream");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("unmarshal"), "method unmarshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
