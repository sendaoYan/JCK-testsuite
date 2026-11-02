/*
 * @test
 * @summary com.sun.xml.internal.ws.util.exception.JAXWSExceptionBase conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXWSExceptionBaseTest
 */

import java.lang.reflect.Modifier;


public class JAXWSExceptionBaseTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.util.exception.JAXWSExceptionBase", false, JAXWSExceptionBaseTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.util.exception.JAXWSExceptionBase"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("javax.xml.ws.WebServiceException"), "superclass javax.xml.ws.WebServiceException");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.istack.internal.localization.Localizable"), "implements com.sun.istack.internal.localization.Localizable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getArguments"), "method getArguments");
        check(mn.contains("getDefaultResourceBundleName"), "method getDefaultResourceBundleName");
        check(mn.contains("getKey"), "method getKey");
        check(mn.contains("getMessage"), "method getMessage");
        check(mn.contains("getResourceBundleName"), "method getResourceBundleName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
