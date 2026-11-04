/*
 * @test
 * @summary org.omg.DynamicAny.DynUnion conformance: interface shape.
 * @run main DynUnionTest
 */

public class DynUnionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynUnion");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynUnionOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynUnionOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
