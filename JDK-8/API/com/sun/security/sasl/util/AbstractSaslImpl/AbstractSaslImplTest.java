/*
 * @test
 * @summary com.sun.security.sasl.util.AbstractSaslImpl conformance: hierarchy and declared members via reflection.
 * @run main AbstractSaslImplTest
 */

public class AbstractSaslImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.sasl.util.AbstractSaslImpl", false, null);
        check(c.getName().equals("com.sun.security.sasl.util.AbstractSaslImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("combineMasks"), "declares combineMasks");
        check(ms.contains("findPreferredMask"), "declares findPreferredMask");
        check(ms.contains("getNegotiatedProperty"), "declares getNegotiatedProperty");
        check(ms.contains("intToNetworkByteOrder"), "declares intToNetworkByteOrder");
        check(ms.contains("isComplete"), "declares isComplete");
        check(ms.contains("networkByteOrderToInt"), "declares networkByteOrderToInt");
        check(ms.contains("parseProp"), "declares parseProp");
        check(ms.contains("parseQop"), "declares parseQop");
        check(ms.contains("parseStrength"), "declares parseStrength");
        check(ms.contains("traceOutput"), "declares traceOutput");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
