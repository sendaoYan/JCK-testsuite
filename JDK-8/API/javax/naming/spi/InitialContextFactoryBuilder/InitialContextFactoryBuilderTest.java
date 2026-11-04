/*
 * @test
 * @summary javax.naming.spi.InitialContextFactoryBuilder conformance: interface hierarchy and members.
 * @run main InitialContextFactoryBuilderTest
 */


public class InitialContextFactoryBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.InitialContextFactoryBuilder");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createInitialContextFactory"), "has method createInitialContextFactory");        System.out.println("PASS (" + passed + " assertions)");
    }
}
