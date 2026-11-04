/*
 * @test
 * @summary com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DeploymentDescriptorParserTest
 */

import java.lang.reflect.Modifier;


public class DeploymentDescriptorParserTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser", false, DeploymentDescriptorParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("configureExternalMetadataReader"), "method configureExternalMetadataReader");
        check(mn.contains("fail"), "method fail");
        check(mn.contains("failWithFullName"), "method failWithFullName");
        check(mn.contains("failWithLocalName"), "method failWithLocalName");
        check(mn.contains("getAttribute"), "method getAttribute");
        check(mn.contains("getBindingIdForToken"), "method getBindingIdForToken");
        check(mn.contains("getMandatoryAttribute"), "method getMandatoryAttribute");
        check(mn.contains("getMandatoryNonEmptyAttribute"), "method getMandatoryNonEmptyAttribute");
        check(mn.contains("getNonEmptyAttribute"), "method getNonEmptyAttribute");
        check(mn.contains("getQNameAttribute"), "method getQNameAttribute");
        check(mn.contains("loadClass"), "method loadClass");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("setHandlersAndRoles"), "method setHandlersAndRoles");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ATTRVALUE_SUPPORTED_VERSIONS"), "field ATTRVALUE_SUPPORTED_VERSIONS");
        check(fn.contains("ATTR_BINDING"), "field ATTR_BINDING");
        check(fn.contains("ATTR_DATABINDING"), "field ATTR_DATABINDING");
        check(fn.contains("ATTR_ENABLE_MTOM"), "field ATTR_ENABLE_MTOM");
        check(fn.contains("ATTR_FILE"), "field ATTR_FILE");
        check(fn.contains("ATTR_IMPLEMENTATION"), "field ATTR_IMPLEMENTATION");
        check(fn.contains("ATTR_MTOM_THRESHOLD_VALUE"), "field ATTR_MTOM_THRESHOLD_VALUE");
        check(fn.contains("ATTR_NAME"), "field ATTR_NAME");
        check(fn.contains("ATTR_PORT"), "field ATTR_PORT");
        check(fn.contains("ATTR_RESOURCE"), "field ATTR_RESOURCE");
        check(fn.contains("ATTR_SERVICE"), "field ATTR_SERVICE");
        check(fn.contains("ATTR_URL_PATTERN"), "field ATTR_URL_PATTERN");
        check(fn.contains("ATTR_VERSION"), "field ATTR_VERSION");
        check(fn.contains("ATTR_WSDL"), "field ATTR_WSDL");
        check(fn.contains("JAXWS_WSDL_DD_DIR"), "field JAXWS_WSDL_DD_DIR");
        check(fn.contains("NS_RUNTIME"), "field NS_RUNTIME");
        check(fn.contains("QNAME_ENDPOINT"), "field QNAME_ENDPOINT");
        check(fn.contains("QNAME_ENDPOINTS"), "field QNAME_ENDPOINTS");
        check(fn.contains("QNAME_EXT_METADA"), "field QNAME_EXT_METADA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
