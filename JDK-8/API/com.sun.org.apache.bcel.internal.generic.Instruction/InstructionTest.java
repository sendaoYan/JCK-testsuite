/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.Instruction conformance: reflection.
 * @run main InstructionTest
 */

public class InstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.Instruction", false, InstructionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Instruction"), "simple name Instruction");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "dump"), "method dump");
        check(has(c, "getName"), "method getName");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        check(has(c, "readInstruction"), "method readInstruction");
        check(has(c, "consumeStack"), "method consumeStack");
        check(has(c, "produceStack"), "method produceStack");
        check(has(c, "getOpcode"), "method getOpcode");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "accept"), "method accept");
        check(has(c, "getComparator"), "method getComparator");
        check(has(c, "setComparator"), "method setComparator");
        check(has(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
