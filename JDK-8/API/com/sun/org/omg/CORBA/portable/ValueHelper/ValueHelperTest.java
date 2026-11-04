/*
 * @test
 * @summary com.sun.org.omg.CORBA.portable.ValueHelper conformance: reflection.
 * @run main ValueHelperTest
 */

public class ValueHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.CORBA.portable.ValueHelper", false, ValueHelperTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ValueHelper"), "simple name ValueHelper");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.portable.BoxedValueHelper"), "interface org.omg.CORBA.portable.BoxedValueHelper");
        check(has(c, "get_class"), "method get_class");
        check(has(c, "get_truncatable_base_ids"), "method get_truncatable_base_ids");
        check(has(c, "get_type"), "method get_type");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
