/*
 * @test
 * @summary com.sun.xml.internal.ws.model.AbstractSEIModelImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AbstractSEIModelImplTest
 */

import java.lang.reflect.Modifier;


public class AbstractSEIModelImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.AbstractSEIModelImpl", false, AbstractSEIModelImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.AbstractSEIModelImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.SEIModel"), "implements com.sun.xml.internal.ws.api.model.SEIModel");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAdditionalClasses"), "method addAdditionalClasses");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("getBindingContext"), "method getBindingContext");
        check(mn.contains("getBoundPortTypeName"), "method getBoundPortTypeName");
        check(mn.contains("getBridge"), "method getBridge");
        check(mn.contains("getContractClass"), "method getContractClass");
        check(mn.contains("getDatabinding"), "method getDatabinding");
        check(mn.contains("getEndpointClass"), "method getEndpointClass");
        check(mn.contains("getJAXBContext"), "method getJAXBContext");
        check(mn.contains("getJavaMethod"), "method getJavaMethod");
        check(mn.contains("getJavaMethodForWsdlOperation"), "method getJavaMethodForWsdlOperation");
        check(mn.contains("getJavaMethods"), "method getJavaMethods");
        check(mn.contains("getKnownNamespaceURIs"), "method getKnownNamespaceURIs");
        check(mn.contains("getMarshallerPool"), "method getMarshallerPool");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("getQNameForJM"), "method getQNameForJM");
        check(mn.contains("getServiceQName"), "method getServiceQName");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("getWSBinding"), "method getWSBinding");
        check(mn.contains("getWSDLLocation"), "method getWSDLLocation");
        check(mn.contains("getXMLBridge"), "method getXMLBridge");
        check(mn.contains("isCheckedException"), "method isCheckedException");
        check(mn.contains("isKnownFault"), "method isKnownFault");
        check(mn.contains("populateMaps"), "method populateMaps");
        check(mn.contains("setDatabinding"), "method setDatabinding");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("bindingContext"), "field bindingContext");
        check(fn.contains("classLoader"), "field classLoader");
        check(fn.contains("contractClass"), "field contractClass");
        check(fn.contains("databindingInfo"), "field databindingInfo");
        check(fn.contains("defaultSchemaNamespaceSuffix"), "field defaultSchemaNamespaceSuffix");
        check(fn.contains("emptyBodyName"), "field emptyBodyName");
        check(fn.contains("endpointClass"), "field endpointClass");
        check(fn.contains("jaxbContext"), "field jaxbContext");
        check(fn.contains("wsBinding"), "field wsBinding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
