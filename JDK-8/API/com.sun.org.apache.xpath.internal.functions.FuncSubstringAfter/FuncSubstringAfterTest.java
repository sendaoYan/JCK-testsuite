/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter conformance: reflection.
 * @run main FuncSubstringAfterTest
 */

public class FuncSubstringAfterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter", false, FuncSubstringAfterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FuncSubstringAfter"), "simple name FuncSubstringAfter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.functions.Function2Args"), "superclass com.sun.org.apache.xpath.internal.functions.Function2Args");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "execute"), "method execute");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
