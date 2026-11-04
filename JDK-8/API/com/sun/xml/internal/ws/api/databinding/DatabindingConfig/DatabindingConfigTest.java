/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.DatabindingConfig conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DatabindingConfigTest
 */

import java.lang.reflect.Modifier;


public class DatabindingConfigTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.DatabindingConfig", false, DatabindingConfigTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.DatabindingConfig"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("additionalValueTypes"), "method additionalValueTypes");
        check(mn.contains("getClassLoader"), "method getClassLoader");
        check(mn.contains("getContractClass"), "method getContractClass");
        check(mn.contains("getEndpointClass"), "method getEndpointClass");
        check(mn.contains("getEntityResolver"), "method getEntityResolver");
        check(mn.contains("getFeatures"), "method getFeatures");
        check(mn.contains("getMappingInfo"), "method getMappingInfo");
        check(mn.contains("getMetadataReader"), "method getMetadataReader");
        check(mn.contains("getWSBinding"), "method getWSBinding");
        check(mn.contains("getWsdlPort"), "method getWsdlPort");
        check(mn.contains("getWsdlSource"), "method getWsdlSource");
        check(mn.contains("getWsdlURL"), "method getWsdlURL");
        check(mn.contains("properties"), "method properties");
        check(mn.contains("setClassLoader"), "method setClassLoader");
        check(mn.contains("setContractClass"), "method setContractClass");
        check(mn.contains("setEndpointClass"), "method setEndpointClass");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("setFeatures"), "method setFeatures");
        check(mn.contains("setMappingInfo"), "method setMappingInfo");
        check(mn.contains("setMetadataReader"), "method setMetadataReader");
        check(mn.contains("setWSBinding"), "method setWSBinding");
        check(mn.contains("setWsdlPort"), "method setWsdlPort");
        check(mn.contains("setWsdlSource"), "method setWsdlSource");
        check(mn.contains("setWsdlURL"), "method setWsdlURL");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("additionalValueTypes"), "field additionalValueTypes");
        check(fn.contains("classLoader"), "field classLoader");
        check(fn.contains("contractClass"), "field contractClass");
        check(fn.contains("endpointClass"), "field endpointClass");
        check(fn.contains("entityResolver"), "field entityResolver");
        check(fn.contains("features"), "field features");
        check(fn.contains("mappingInfo"), "field mappingInfo");
        check(fn.contains("metadataReader"), "field metadataReader");
        check(fn.contains("properties"), "field properties");
        check(fn.contains("wsBinding"), "field wsBinding");
        check(fn.contains("wsdlPort"), "field wsdlPort");
        check(fn.contains("wsdlSource"), "field wsdlSource");
        check(fn.contains("wsdlURL"), "field wsdlURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
