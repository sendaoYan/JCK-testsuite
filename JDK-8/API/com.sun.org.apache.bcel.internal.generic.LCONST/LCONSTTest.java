/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.LCONST conformance: reflection.
 * @run main LCONSTTest
 */

public class LCONSTTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.LCONST", false, LCONSTTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LCONST"), "simple name LCONST");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Instruction"), "superclass com.sun.org.apache.bcel.internal.generic.Instruction");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction"), "interface com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(has(c, "getValue"), "method getValue");
        check(has(c, "getType"), "method getType");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
