/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.WsaServerTube conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WsaServerTubeTest
 */

import java.lang.reflect.Modifier;


public class WsaServerTubeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.WsaServerTube", false, WsaServerTubeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.WsaServerTube"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.addressing.WsaTube"), "superclass com.sun.xml.internal.ws.addressing.WsaTube");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("checkAnonymousSemantics"), "method checkAnonymousSemantics");
        check(mn.contains("checkMessageAddressingProperties"), "method checkMessageAddressingProperties");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("isAnonymousRequired"), "method isAnonymousRequired");
        check(mn.contains("processException"), "method processException");
        check(mn.contains("processRequest"), "method processRequest");
        check(mn.contains("processResponse"), "method processResponse");
        check(mn.contains("validateAction"), "method validateAction");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("REQUEST_MESSAGE_ID"), "field REQUEST_MESSAGE_ID");
        check(fn.contains("isEarlyBackchannelCloseAllowed"), "field isEarlyBackchannelCloseAllowed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
