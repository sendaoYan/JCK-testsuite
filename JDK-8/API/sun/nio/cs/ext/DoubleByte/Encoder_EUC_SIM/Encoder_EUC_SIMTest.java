/*
 * @test
 * @summary sun.nio.cs.ext.DoubleByte.Encoder_EUC_SIM conformance: class and members.
 * @run main Encoder_EUC_SIMTest
 */

public class Encoder_EUC_SIMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.DoubleByte$Encoder_EUC_SIM", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.DoubleByte$Encoder_EUC_SIM"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.DoubleByte$Encoder"), "superclass sun.nio.cs.ext.DoubleByte$Encoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
