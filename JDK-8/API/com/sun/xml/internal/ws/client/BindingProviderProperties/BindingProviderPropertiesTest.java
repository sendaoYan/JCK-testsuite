/*
 * @test
 * @summary com.sun.xml.internal.ws.client.BindingProviderProperties conformance: reflection (kind, superclass, interfaces, methods).
 * @run main BindingProviderPropertiesTest
 */

import java.lang.reflect.Modifier;


public class BindingProviderPropertiesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.BindingProviderProperties", false, BindingProviderPropertiesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.BindingProviderProperties"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.developer.JAXWSProperties"), "implements com.sun.xml.internal.ws.developer.JAXWSProperties");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("HOSTNAME_VERIFICATION_PROPERTY"), "field HOSTNAME_VERIFICATION_PROPERTY");
        check(fn.contains("HTTP_COOKIE_JAR"), "field HTTP_COOKIE_JAR");
        check(fn.contains("JAXWS_CLIENT_HANDLE_PROPERTY"), "field JAXWS_CLIENT_HANDLE_PROPERTY");
        check(fn.contains("JAXWS_HANDLER_CONFIG"), "field JAXWS_HANDLER_CONFIG");
        check(fn.contains("ONE_WAY_OPERATION"), "field ONE_WAY_OPERATION");
        check(fn.contains("REDIRECT_REQUEST_PROPERTY"), "field REDIRECT_REQUEST_PROPERTY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
