/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.InstructionFinder conformance: reflection.
 * @run main InstructionFinderTest
 */

public class InstructionFinderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.util.InstructionFinder", false, InstructionFinderTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("InstructionFinder"), "simple name InstructionFinder");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "reread"), "method reread");
        check(has(c, "search"), "method search");
        check(has(c, "getInstructionList"), "method getInstructionList");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
