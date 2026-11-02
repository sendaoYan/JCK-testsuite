/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.MappingInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MappingInfoTest
 */

import java.lang.reflect.Modifier;


public class MappingInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.MappingInfo", false, MappingInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.MappingInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBindingID"), "method getBindingID");
        check(mn.contains("getDatabindingMode"), "method getDatabindingMode");
        check(mn.contains("getDefaultSchemaNamespaceSuffix"), "method getDefaultSchemaNamespaceSuffix");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("getSoapBodyStyle"), "method getSoapBodyStyle");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("setBindingID"), "method setBindingID");
        check(mn.contains("setDatabindingMode"), "method setDatabindingMode");
        check(mn.contains("setDefaultSchemaNamespaceSuffix"), "method setDefaultSchemaNamespaceSuffix");
        check(mn.contains("setPortName"), "method setPortName");
        check(mn.contains("setServiceName"), "method setServiceName");
        check(mn.contains("setSoapBodyStyle"), "method setSoapBodyStyle");
        check(mn.contains("setTargetNamespace"), "method setTargetNamespace");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("bindingID"), "field bindingID");
        check(fn.contains("databindingMode"), "field databindingMode");
        check(fn.contains("defaultSchemaNamespaceSuffix"), "field defaultSchemaNamespaceSuffix");
        check(fn.contains("portName"), "field portName");
        check(fn.contains("serviceName"), "field serviceName");
        check(fn.contains("soapBodyStyle"), "field soapBodyStyle");
        check(fn.contains("targetNamespace"), "field targetNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
