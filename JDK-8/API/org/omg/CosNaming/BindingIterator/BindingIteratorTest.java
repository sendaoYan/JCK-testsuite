/*
 * @test
 * @summary org.omg.CosNaming.BindingIterator conformance: interface shape.
 * @run main BindingIteratorTest
 */

public class BindingIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.BindingIterator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CosNaming.BindingIteratorOperations.class.isAssignableFrom(c), "extends org.omg.CosNaming.BindingIteratorOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (batch C, jdk8u372 probed): iterator surface ----
        check(org.omg.CosNaming.BindingIteratorOperations.class.isAssignableFrom(org.omg.CosNaming.BindingIterator.class),
              "extends BindingIteratorOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CosNaming.BindingIterator.class), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.BindingIterator.class), "extends IDLEntity");
        try {
            java.lang.reflect.Method m1 = org.omg.CosNaming.BindingIterator.class.getMethod("next_one", org.omg.CosNaming.BindingHolder.class);
            check(m1.getReturnType() == boolean.class, "next_one returns boolean");
            java.lang.reflect.Method m2 = org.omg.CosNaming.BindingIterator.class.getMethod("next_n", int.class, org.omg.CosNaming.BindingListHolder.class);
            check(m2.getReturnType() == boolean.class, "next_n returns boolean");
            org.omg.CosNaming.BindingIterator.class.getMethod("destroy");
            check(true, "destroy() present");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("BindingIterator surface incomplete");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
