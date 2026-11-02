/*
 * @test
 * @summary com.sun.org.omg.CORBA._IDLTypeStub conformance: reflection.
 * @run main _IDLTypeStubTest
 */

public class _IDLTypeStubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.CORBA._IDLTypeStub", false, _IDLTypeStubTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("_IDLTypeStub"), "simple name _IDLTypeStub");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ObjectImpl"), "superclass org.omg.CORBA.portable.ObjectImpl");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.IDLType"), "interface org.omg.CORBA.IDLType");
        check(has(c, "type"), "method type");
        check(has(c, "def_kind"), "method def_kind");
        check(has(c, "destroy"), "method destroy");
        check(has(c, "_ids"), "method _ids");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
