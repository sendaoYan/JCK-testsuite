/*
 * @test
 * @summary com.sun.org.omg.SendingContext.CodeBase conformance: reflection.
 * @run main CodeBaseTest
 */

public class CodeBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.SendingContext.CodeBase", false, CodeBaseTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CodeBase"), "simple name CodeBase");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.omg.SendingContext.CodeBaseOperations"), "interface com.sun.org.omg.SendingContext.CodeBaseOperations");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.SendingContext.RunTime"), "interface org.omg.SendingContext.RunTime");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.portable.IDLEntity"), "interface org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
