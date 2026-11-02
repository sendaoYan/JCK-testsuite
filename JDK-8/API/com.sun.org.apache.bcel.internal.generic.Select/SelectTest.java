/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.Select conformance: reflection.
 * @run main SelectTest
 */

public class SelectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.Select", false, SelectTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Select"), "simple name Select");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.BranchInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.BranchInstruction");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction"), "interface com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackProducer"), "interface com.sun.org.apache.bcel.internal.generic.StackProducer");
        check(has(c, "dump"), "method dump");
        check(has(c, "toString"), "method toString");
        check(has(c, "setTarget"), "method setTarget");
        check(has(c, "updateTarget"), "method updateTarget");
        check(has(c, "containsTarget"), "method containsTarget");
        check(has(c, "getMatchs"), "method getMatchs");
        check(has(c, "getIndices"), "method getIndices");
        check(has(c, "getTargets"), "method getTargets");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
