/*
 * @test
 * @summary javax.jws.soap.SOAPBinding.ParameterStyle conformance: enum constants and methods.
 * @run main ParameterStyleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ParameterStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.jws.soap.SOAPBinding$ParameterStyle");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("BARE"), "constant BARE");
        check(cn.contains("WRAPPED"), "constant WRAPPED");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
