/*
 * @test
 * @summary org.omg.CORBA.portable.IDLEntity conformance: interface shape.
 * @run main IDLEntityTest
 */

public class IDLEntityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.IDLEntity");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends java.io.Serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
