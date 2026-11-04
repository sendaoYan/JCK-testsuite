/*
 * @test
 * @summary com.sun.xml.internal.ws.db.DatabindingImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DatabindingImplTest
 */

import java.lang.reflect.Modifier;


public class DatabindingImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.db.DatabindingImpl", false, DatabindingImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.db.DatabindingImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.databinding.Databinding"), "implements com.sun.xml.internal.ws.api.databinding.Databinding");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createJavaCallInfo"), "method createJavaCallInfo");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("deserializeRequest"), "method deserializeRequest");
        check(mn.contains("deserializeResponse"), "method deserializeResponse");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("generateWSDL"), "method generateWSDL");
        check(mn.contains("getClientBridge"), "method getClientBridge");
        check(mn.contains("getEndpointBridge"), "method getEndpointBridge");
        check(mn.contains("getFeatures"), "method getFeatures");
        check(mn.contains("getMessageContextFactory"), "method getMessageContextFactory");
        check(mn.contains("getModel"), "method getModel");
        check(mn.contains("serializeRequest"), "method serializeRequest");
        check(mn.contains("serializeResponse"), "method serializeResponse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
