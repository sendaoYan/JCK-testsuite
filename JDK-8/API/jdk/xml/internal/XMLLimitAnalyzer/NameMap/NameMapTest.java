/*
 * @test
 * @summary jdk.xml.internal.XMLLimitAnalyzer.NameMap conformance: enum constants and methods.
 * @run main NameMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NameMapTest {
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLLimitAnalyzer$NameMap");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("ENTITY_EXPANSION_LIMIT"), "constant ENTITY_EXPANSION_LIMIT");
        check(cn.contains("MAX_OCCUR_NODE_LIMIT"), "constant MAX_OCCUR_NODE_LIMIT");
        check(cn.contains("ELEMENT_ATTRIBUTE_LIMIT"), "constant ELEMENT_ATTRIBUTE_LIMIT");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
