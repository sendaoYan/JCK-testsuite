/*
 * @test
 * @summary com.sun.xml.internal.ws.api.addressing.OneWayFeature conformance: reflection (kind, superclass, interfaces, methods).
 * @run main OneWayFeatureTest
 */

import java.lang.reflect.Modifier;


public class OneWayFeatureTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.addressing.OneWayFeature", false, OneWayFeatureTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.addressing.OneWayFeature"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.xml.ws.WebServiceFeature"), "superclass javax.xml.ws.WebServiceFeature");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("enableSslForEpr"), "method enableSslForEpr");
        check(mn.contains("getFaultTo"), "method getFaultTo");
        check(mn.contains("getFrom"), "method getFrom");
        check(mn.contains("getID"), "method getID");
        check(mn.contains("getMessageId"), "method getMessageId");
        check(mn.contains("getRelatesToID"), "method getRelatesToID");
        check(mn.contains("getReplyTo"), "method getReplyTo");
        check(mn.contains("getSslFaultTo"), "method getSslFaultTo");
        check(mn.contains("getSslReplyTo"), "method getSslReplyTo");
        check(mn.contains("hasSslEprs"), "method hasSslEprs");
        check(mn.contains("isUseAsyncWithSyncInvoke"), "method isUseAsyncWithSyncInvoke");
        check(mn.contains("setFaultTo"), "method setFaultTo");
        check(mn.contains("setFrom"), "method setFrom");
        check(mn.contains("setRelatesToID"), "method setRelatesToID");
        check(mn.contains("setReplyTo"), "method setReplyTo");
        check(mn.contains("setSslFaultTo"), "method setSslFaultTo");
        check(mn.contains("setSslReplyTo"), "method setSslReplyTo");
        check(mn.contains("setUseAsyncWithSyncInvoke"), "method setUseAsyncWithSyncInvoke");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ID"), "field ID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
