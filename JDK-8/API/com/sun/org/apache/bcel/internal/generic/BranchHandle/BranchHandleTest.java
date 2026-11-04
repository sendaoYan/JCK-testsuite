/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.BranchHandle conformance: reflection.
 * @run main BranchHandleTest
 */

public class BranchHandleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.BranchHandle", false, BranchHandleTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BranchHandle"), "simple name BranchHandle");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.InstructionHandle"), "superclass com.sun.org.apache.bcel.internal.generic.InstructionHandle");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "setTarget"), "method setTarget");
        check(has(c, "updateTarget"), "method updateTarget");
        check(has(c, "getTarget"), "method getTarget");
        check(has(c, "setInstruction"), "method setInstruction");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
