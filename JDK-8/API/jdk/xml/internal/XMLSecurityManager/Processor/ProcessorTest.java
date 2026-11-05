/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.Processor conformance: enum constants and methods.
 * @run main ProcessorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ProcessorTest {
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Processor");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("PARSER"), "constant PARSER");
        check(cn.contains("XPATH"), "constant XPATH");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");

        // ---- declaration order, ordinals, enum basics ----
        Object[] order = (Object[]) c.getMethod("values").invoke(null);
        check(((java.lang.Enum<?>) order[0]).name().equals("PARSER"), "ordinal 0 PARSER");
        check(((java.lang.Enum<?>) order[1]).name().equals("XPATH"), "ordinal 1 XPATH");
        check(((java.lang.Enum<?>) order[0]).ordinal() == 0, "PARSER ordinal 0");
        check(((java.lang.Enum<?>) order[1]).ordinal() == 1, "XPATH ordinal 1");
        check("PARSER".equals(((java.lang.Enum<?>) order[0]).toString()), "toString PARSER");
        check("XPATH".equals(((java.lang.Enum<?>) order[1]).toString()), "toString XPATH");
        check(((java.lang.Enum) order[0]).compareTo((java.lang.Enum) order[1]) < 0, "PARSER < XPATH");
        check(c.getEnumConstants().length == 2, "exactly two processors");

        // ---- valueOf round-trip; unknown rejected ----
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        check(valueOf.invoke(null, "PARSER") == order[0], "valueOf round-trip PARSER");
        check(valueOf.invoke(null, "XPATH") == order[1], "valueOf round-trip XPATH");
        try {
            valueOf.invoke(null, "TRANSFORMER");
            check(false, "valueOf unknown must throw");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException, "valueOf unknown -> IllegalArgumentException");
        }

        // ---- linkage: every Limit reports support for exactly one processor ----
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        java.lang.reflect.Method isSupported = limC.getMethod("isSupported", c);
        Object[] limits = limC.getEnumConstants();
        for (int i = 0; i < limits.length; i++) {
            String n = ((java.lang.Enum<?>) limits[i]).name();
            boolean parserOk = ((Boolean) isSupported.invoke(limits[i], order[0])).booleanValue();
            boolean xpathOk = ((Boolean) isSupported.invoke(limits[i], order[1])).booleanValue();
            check(parserOk != xpathOk, n + " supported by exactly one processor");
            check(parserOk == (i < 9), n + (i < 9 ? " belongs to PARSER" : " belongs to XPATH"));
        }

        // ---- linkage: XPathFactory compilation exercises the XPATH processor limits ----
        try {
            javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("1+1");
            check(true, "XPATH processor path exercised via javax.xml.xpath");
        } catch (Throwable t) {
            check(false, "small xpath expression must compile: " + t);
        }
        try {
            javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("a|b|c");
            check(true, "union expression compiles under default limits");
        } catch (Throwable t) {
            check(false, "union expression must compile: " + t);
        }
        // XPATH_OP_LIMIT (default 100) enforced on the XPath compilation path
        StringBuilder big = new StringBuilder("1");
        for (int i = 0; i < 300; i++) big.append("+1");
        try {
            javax.xml.xpath.XPathFactory.newInstance().newXPath().compile(big.toString());
            check(false, "operator count over 100 must be rejected");
        } catch (javax.xml.xpath.XPathExpressionException xe) {
            check(xe.getMessage() != null && xe.getMessage().contains("'100'"), "op limit cited in failure: " + xe.getMessage());
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
