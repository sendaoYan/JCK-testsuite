/*
 * @test
 * @summary com.sun.xml.internal.ws.model.CheckedExceptionImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main CheckedExceptionImplTest
 */

import java.lang.reflect.Modifier;


public class CheckedExceptionImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.CheckedExceptionImpl", false, CheckedExceptionImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.CheckedExceptionImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.CheckedException"), "implements com.sun.xml.internal.ws.api.model.CheckedException");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBond"), "method getBond");
        check(mn.contains("getBridge"), "method getBridge");
        check(mn.contains("getDefaultFaultAction"), "method getDefaultFaultAction");
        check(mn.contains("getDetailBean"), "method getDetailBean");
        check(mn.contains("getDetailType"), "method getDetailType");
        check(mn.contains("getExceptionClass"), "method getExceptionClass");
        check(mn.contains("getExceptionType"), "method getExceptionType");
        check(mn.contains("getFaultAction"), "method getFaultAction");
        check(mn.contains("getMessageName"), "method getMessageName");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("setFaultAction"), "method setFaultAction");
        check(mn.contains("setMessageName"), "method setMessageName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
