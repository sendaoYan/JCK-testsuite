/*
 * @test
 * @summary com.sun.org.omg.SendingContext._CodeBaseStub conformance: reflection.
 * @run main _CodeBaseStubTest
 */

public class _CodeBaseStubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.SendingContext._CodeBaseStub", false, _CodeBaseStubTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("_CodeBaseStub"), "simple name _CodeBaseStub");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ObjectImpl"), "superclass org.omg.CORBA.portable.ObjectImpl");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.omg.SendingContext.CodeBase"), "interface com.sun.org.omg.SendingContext.CodeBase");
        check(has(c, "get_ir"), "method get_ir");
        check(has(c, "implementation"), "method implementation");
        check(has(c, "implementations"), "method implementations");
        check(has(c, "meta"), "method meta");
        check(has(c, "metas"), "method metas");
        check(has(c, "bases"), "method bases");
        check(has(c, "_ids"), "method _ids");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
