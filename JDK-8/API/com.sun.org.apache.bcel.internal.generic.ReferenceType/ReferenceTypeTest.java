/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ReferenceType conformance: reflection.
 * @run main ReferenceTypeTest
 */

public class ReferenceTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ReferenceType", false, ReferenceTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ReferenceType"), "simple name ReferenceType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Type"), "superclass com.sun.org.apache.bcel.internal.generic.Type");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "isCastableTo"), "method isCastableTo");
        check(has(c, "isAssignmentCompatibleWith"), "method isAssignmentCompatibleWith");
        check(has(c, "getFirstCommonSuperclass"), "method getFirstCommonSuperclass");
        check(has(c, "firstCommonSuperclass"), "method firstCommonSuperclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
