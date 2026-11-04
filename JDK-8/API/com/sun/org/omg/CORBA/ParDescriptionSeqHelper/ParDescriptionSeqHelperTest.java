/*
 * @test
 * @summary com.sun.org.omg.CORBA.ParDescriptionSeqHelper conformance: reflection.
 * @run main ParDescriptionSeqHelperTest
 */

public class ParDescriptionSeqHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.CORBA.ParDescriptionSeqHelper", false, ParDescriptionSeqHelperTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ParDescriptionSeqHelper"), "simple name ParDescriptionSeqHelper");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "insert"), "method insert");
        check(has(c, "extract"), "method extract");
        check(has(c, "type"), "method type");
        check(has(c, "id"), "method id");
        check(has(c, "read"), "method read");
        check(has(c, "write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
