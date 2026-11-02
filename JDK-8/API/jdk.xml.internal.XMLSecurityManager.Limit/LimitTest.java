/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.Limit conformance: enum constants and methods.
 * @run main LimitTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class LimitTest {
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 12, "12 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("ENTITY_EXPANSION_LIMIT"), "constant ENTITY_EXPANSION_LIMIT");
        check(cn.contains("MAX_OCCUR_NODE_LIMIT"), "constant MAX_OCCUR_NODE_LIMIT");
        check(cn.contains("ELEMENT_ATTRIBUTE_LIMIT"), "constant ELEMENT_ATTRIBUTE_LIMIT");
        check(cn.contains("TOTAL_ENTITY_SIZE_LIMIT"), "constant TOTAL_ENTITY_SIZE_LIMIT");
        check(cn.contains("GENERAL_ENTITY_SIZE_LIMIT"), "constant GENERAL_ENTITY_SIZE_LIMIT");
        check(cn.contains("PARAMETER_ENTITY_SIZE_LIMIT"), "constant PARAMETER_ENTITY_SIZE_LIMIT");
        check(cn.contains("MAX_ELEMENT_DEPTH_LIMIT"), "constant MAX_ELEMENT_DEPTH_LIMIT");
        check(cn.contains("MAX_NAME_LIMIT"), "constant MAX_NAME_LIMIT");
        check(cn.contains("ENTITY_REPLACEMENT_LIMIT"), "constant ENTITY_REPLACEMENT_LIMIT");
        check(cn.contains("XPATH_GROUP_LIMIT"), "constant XPATH_GROUP_LIMIT");
        check(cn.contains("XPATH_OP_LIMIT"), "constant XPATH_OP_LIMIT");
        check(cn.contains("XPATH_TOTALOP_LIMIT"), "constant XPATH_TOTALOP_LIMIT");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");
        check(names.contains("equalsAPIPropertyName"), "has method equalsAPIPropertyName");
        check(names.contains("equalsSystemPropertyName"), "has method equalsSystemPropertyName");
        check(names.contains("key"), "has method key");
        check(names.contains("apiProperty"), "has method apiProperty");
        check(names.contains("defaultValue"), "has method defaultValue");
        check(names.contains("isSupported"), "has method isSupported");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
