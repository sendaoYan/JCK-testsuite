/*
 * @test
 * @summary com.sun.org.omg.SendingContext._CodeBaseImplBase conformance: reflection.
 * @run main _CodeBaseImplBaseTest
 */

public class _CodeBaseImplBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.SendingContext._CodeBaseImplBase", false, _CodeBaseImplBaseTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("_CodeBaseImplBase"), "simple name _CodeBaseImplBase");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ObjectImpl"), "superclass org.omg.CORBA.portable.ObjectImpl");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.omg.SendingContext.CodeBase"), "interface com.sun.org.omg.SendingContext.CodeBase");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.portable.InvokeHandler"), "interface org.omg.CORBA.portable.InvokeHandler");
        check(has(c, "_invoke"), "method _invoke");
        check(has(c, "_ids"), "method _ids");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
