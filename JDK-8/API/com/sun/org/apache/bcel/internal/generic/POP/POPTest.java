/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.POP conformance: reflection.
 * @run main POPTest
 */

public class POPTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.POP", false, POPTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("POP"), "simple name POP");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.StackInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.StackInstruction");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.PopInstruction"), "interface com.sun.org.apache.bcel.internal.generic.PopInstruction");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
