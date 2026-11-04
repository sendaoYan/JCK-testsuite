/*
 * @test
 * @summary com.sun.org.omg.CORBA.Repository conformance: reflection.
 * @run main RepositoryTest
 */

public class RepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.CORBA.Repository", false, RepositoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Repository"), "simple name Repository");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.IRObject"), "interface org.omg.CORBA.IRObject");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.portable.IDLEntity"), "interface org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
