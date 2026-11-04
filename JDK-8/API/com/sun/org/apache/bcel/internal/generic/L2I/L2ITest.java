/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.L2I conformance: reflection.
 * @run main L2ITest
 */

public class L2ITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.L2I", false, L2ITest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("L2I"), "simple name L2I");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.ConversionInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.ConversionInstruction");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
