/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.Parameter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ParameterTest
 */

import java.lang.reflect.Modifier;


public class ParameterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.Parameter", false, ParameterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.Parameter"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getBridge"), "method getBridge");
        check(mn.contains("getHolderValue"), "method getHolderValue");
        check(mn.contains("getInBinding"), "method getInBinding");
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("getMode"), "method getMode");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getOutBinding"), "method getOutBinding");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("getPartName"), "method getPartName");
        check(mn.contains("isIN"), "method isIN");
        check(mn.contains("isINOUT"), "method isINOUT");
        check(mn.contains("isOUT"), "method isOUT");
        check(mn.contains("isResponse"), "method isResponse");
        check(mn.contains("isReturnValue"), "method isReturnValue");
        check(mn.contains("isWrapperStyle"), "method isWrapperStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
