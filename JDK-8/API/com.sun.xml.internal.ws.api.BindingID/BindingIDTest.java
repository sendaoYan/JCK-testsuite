/*
 * @test
 * @summary com.sun.xml.internal.ws.api.BindingID conformance: reflection (kind, superclass, interfaces, methods).
 * @run main BindingIDTest
 */

import java.lang.reflect.Modifier;


public class BindingIDTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.BindingID", false, BindingIDTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.BindingID"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("canGenerateWSDL"), "method canGenerateWSDL");
        check(mn.contains("createBinding"), "method createBinding");
        check(mn.contains("createBuiltinFeatureList"), "method createBuiltinFeatureList");
        check(mn.contains("createEncoder"), "method createEncoder");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getParameter"), "method getParameter");
        check(mn.contains("getSOAPVersion"), "method getSOAPVersion");
        check(mn.contains("getTransport"), "method getTransport");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("SOAP11_HTTP"), "field SOAP11_HTTP");
        check(fn.contains("SOAP11_HTTP_MTOM"), "field SOAP11_HTTP_MTOM");
        check(fn.contains("SOAP12_HTTP"), "field SOAP12_HTTP");
        check(fn.contains("SOAP12_HTTP_MTOM"), "field SOAP12_HTTP_MTOM");
        check(fn.contains("XML_HTTP"), "field XML_HTTP");
        check(fn.contains("X_SOAP12_HTTP"), "field X_SOAP12_HTTP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
