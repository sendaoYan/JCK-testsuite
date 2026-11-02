/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.GotoInstruction conformance: reflection.
 * @run main GotoInstructionTest
 */

public class GotoInstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.GotoInstruction", false, GotoInstructionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("GotoInstruction"), "simple name GotoInstruction");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.BranchInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.BranchInstruction");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.UnconditionalBranch"), "interface com.sun.org.apache.bcel.internal.generic.UnconditionalBranch");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
