/*
 * @test
 * @summary com.sun.xml.internal.ws.client.PortInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PortInfoTest
 */

import java.lang.reflect.Modifier;


public class PortInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.PortInfo", false, PortInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.PortInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.client.WSPortInfo"), "implements com.sun.xml.internal.ws.api.client.WSPortInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBinding"), "method createBinding");
        check(mn.contains("createPolicyMap"), "method createPolicyMap");
        check(mn.contains("getBindingID"), "method getBindingID");
        check(mn.contains("getBindingId"), "method getBindingId");
        check(mn.contains("getEndpointAddress"), "method getEndpointAddress");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getPolicyMap"), "method getPolicyMap");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getServiceName"), "method getServiceName");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("bindingId"), "field bindingId");
        check(fn.contains("policyMap"), "field policyMap");
        check(fn.contains("portModel"), "field portModel");
        check(fn.contains("portName"), "field portName");
        check(fn.contains("targetEndpoint"), "field targetEndpoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
