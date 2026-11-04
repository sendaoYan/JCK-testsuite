/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction conformance: reflection.
 * @run main ConstantPushInstructionTest
 */

public class ConstantPushInstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction", false, ConstantPushInstructionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ConstantPushInstruction"), "simple name ConstantPushInstruction");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.PushInstruction"), "interface com.sun.org.apache.bcel.internal.generic.PushInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(has(c, "getValue"), "method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
