/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.BranchInstruction conformance: reflection.
 * @run main BranchInstructionTest
 */

public class BranchInstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.BranchInstruction", false, BranchInstructionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BranchInstruction"), "simple name BranchInstruction");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Instruction"), "superclass com.sun.org.apache.bcel.internal.generic.Instruction");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionTargeter"), "interface com.sun.org.apache.bcel.internal.generic.InstructionTargeter");
        check(has(c, "dump"), "method dump");
        check(has(c, "toString"), "method toString");
        check(has(c, "getIndex"), "method getIndex");
        check(has(c, "getTarget"), "method getTarget");
        check(has(c, "setTarget"), "method setTarget");
        check(has(c, "updateTarget"), "method updateTarget");
        check(has(c, "containsTarget"), "method containsTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
