/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ArrayType conformance: reflection.
 * @run main ArrayTypeTest
 */

public class ArrayTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ArrayType", false, ArrayTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ArrayType"), "simple name ArrayType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.ReferenceType"), "superclass com.sun.org.apache.bcel.internal.generic.ReferenceType");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getBasicType"), "method getBasicType");
        check(has(c, "getElementType"), "method getElementType");
        check(has(c, "getDimensions"), "method getDimensions");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
