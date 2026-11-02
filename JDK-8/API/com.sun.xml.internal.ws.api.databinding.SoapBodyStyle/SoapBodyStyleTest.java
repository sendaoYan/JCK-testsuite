/*
 * @test
 * @summary com.sun.xml.internal.ws.api.databinding.SoapBodyStyle conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SoapBodyStyleTest
 */

import java.lang.reflect.Modifier;


public class SoapBodyStyleTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.databinding.SoapBodyStyle", false, SoapBodyStyleTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.databinding.SoapBodyStyle"), "name");
        check(c.isEnum(), "enum");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isBare"), "method isBare");
        check(mn.contains("isDocument"), "method isDocument");
        check(mn.contains("isDocumentWrapper"), "method isDocumentWrapper");
        check(mn.contains("isLiteral"), "method isLiteral");
        check(mn.contains("isRpc"), "method isRpc");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("values"), "method values");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("DocumentBare"), "field DocumentBare");
        check(fn.contains("DocumentWrapper"), "field DocumentWrapper");
        check(fn.contains("RpcEncoded"), "field RpcEncoded");
        check(fn.contains("RpcLiteral"), "field RpcLiteral");
        check(fn.contains("Unspecificed"), "field Unspecificed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
