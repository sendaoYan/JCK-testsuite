/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExt conformance: interface shape.
 * @run main NamingContextExtTest
 */

public class NamingContextExtTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextExt");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CosNaming.NamingContextExtOperations.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContextExtOperations");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContext");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
