/*
 * @test
 * @summary com.sun.xml.internal.ws.api.WSBinding conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSBindingTest
 */

import java.lang.reflect.Modifier;


public class WSBindingTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.WSBinding", false, WSBindingTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.WSBinding"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.Binding"), "implements javax.xml.ws.Binding");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addKnownHeader"), "method addKnownHeader");
        check(mn.contains("getAddressingVersion"), "method getAddressingVersion");
        check(mn.contains("getBindingId"), "method getBindingId");
        check(mn.contains("getFaultMessageFeatures"), "method getFaultMessageFeatures");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getFeatures"), "method getFeatures");
        check(mn.contains("getHandlerChain"), "method getHandlerChain");
        check(mn.contains("getInputMessageFeatures"), "method getInputMessageFeatures");
        check(mn.contains("getKnownHeaders"), "method getKnownHeaders");
        check(mn.contains("getMessageContextFactory"), "method getMessageContextFactory");
        check(mn.contains("getOperationFeature"), "method getOperationFeature");
        check(mn.contains("getOperationFeatures"), "method getOperationFeatures");
        check(mn.contains("getOutputMessageFeatures"), "method getOutputMessageFeatures");
        check(mn.contains("getSOAPVersion"), "method getSOAPVersion");
        check(mn.contains("isFeatureEnabled"), "method isFeatureEnabled");
        check(mn.contains("isOperationFeatureEnabled"), "method isOperationFeatureEnabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
