/*
 * @test
 * @summary org.omg.CORBA.portable.ObjectImpl conformance: abstract class shape.
 * @run main ObjectImplTest
 */

public class ObjectImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.ObjectImpl");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "implements org.omg.CORBA.Object");

        // ---- deepened: subclass channel and delegate-less behavior (jdk8u372 measured) ----
        org.omg.CORBA.portable.ObjectImpl impl = new org.omg.CORBA.portable.ObjectImpl() {
            public String[] _ids() { return new String[]{"IDL:A:1.0", "IDL:B:1.0"}; }
        };
        String[] ids = impl._ids();
        check(ids.length == 2 && "IDL:A:1.0".equals(ids[0]) && "IDL:B:1.0".equals(ids[1]),
              "subclassed _ids() readback");
        try {
            impl._get_delegate();
            check(false, "_get_delegate without delegate should throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_get_delegate() without delegate -> BAD_OPERATION('The delegate has not been set!')");
        }
        try {
            impl._hash(0);
            check(false, "_hash should throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_hash(0) without delegate -> BAD_OPERATION");
        }
        try {
            impl._is_a("IDL:A:1.0");
            check(false, "_is_a should throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_is_a without delegate -> BAD_OPERATION");
        }
        try {
            impl._orb();
            check(false, "_orb should throw");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_orb() without delegate -> BAD_OPERATION");
        }
        check(impl.toString().contains("no delegate set"), "toString reports 'no delegate set'");
        check(impl.equals(impl), "equals(self) true without delegate");
        check(!impl.equals(new org.omg.CORBA.portable.ObjectImpl() {
                public String[] _ids() { return new String[0]; } }),
              "equals(other) false");
        check(impl.hashCode() == impl.hashCode(), "hashCode stable");
        // _ids is the only abstract member
        check(java.util.Arrays.stream(org.omg.CORBA.portable.ObjectImpl.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .allMatch(m -> m.getName().equals("_ids")),
              "_ids is the sole abstract method");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
