/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.JavaCallInfo conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JavaCallInfoTest
 */

import java.lang.reflect.Modifier;


public class JavaCallInfoTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.JavaCallInfo", false, JavaCallInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.JavaCallInfo"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.oracle.webservices.internal.api.databinding.JavaCallInfo"), "implements com.oracle.webservices.internal.api.databinding.JavaCallInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getException"), "method getException");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("getParameters"), "method getParameters");
        check(mn.contains("getReturnValue"), "method getReturnValue");
        check(mn.contains("setException"), "method setException");
        check(mn.contains("setMethod"), "method setMethod");
        check(mn.contains("setParameters"), "method setParameters");
        check(mn.contains("setReturnValue"), "method setReturnValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
