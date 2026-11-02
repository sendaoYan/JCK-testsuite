/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.PUSH conformance: reflection.
 * @run main PUSHTest
 */

public class PUSHTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.PUSH", false, PUSHTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("PUSH"), "simple name PUSH");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.CompoundInstruction"), "interface com.sun.org.apache.bcel.internal.generic.CompoundInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction"), "interface com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionConstants"), "interface com.sun.org.apache.bcel.internal.generic.InstructionConstants");
        check(has(c, "getInstructionList"), "method getInstructionList");
        check(has(c, "getInstruction"), "method getInstruction");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
