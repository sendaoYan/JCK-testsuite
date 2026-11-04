/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.BasicType conformance: reflection.
 * @run main BasicTypeTest
 */

public class BasicTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.BasicType", false, BasicTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BasicType"), "simple name BasicType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Type"), "superclass com.sun.org.apache.bcel.internal.generic.Type");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getType"), "method getType");
        check(has(c, "equals"), "method equals");
        check(has(c, "hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
