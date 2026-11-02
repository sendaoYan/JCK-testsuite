/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils.ECCurveDefinition conformance: class hierarchy and members.
 * @run main ECCurveDefinitionTest
 */
public class ECCurveDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils$ECCurveDefinition", false, ECCurveDefinitionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getOid"), "method getOid");
        check(mn.contains("getField"), "method getField");
        check(mn.contains("getA"), "method getA");
        check(mn.contains("getB"), "method getB");
        check(mn.contains("getX"), "method getX");
        check(mn.contains("getY"), "method getY");
        check(mn.contains("getN"), "method getN");
        check(mn.contains("getH"), "method getH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
