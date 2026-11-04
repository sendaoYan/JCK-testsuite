/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionHandle conformance: reflection.
 * @run main InstructionHandleTest
 */

public class InstructionHandleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionHandle", false, InstructionHandleTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("InstructionHandle"), "simple name InstructionHandle");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "getNext"), "method getNext");
        check(has(c, "getPrev"), "method getPrev");
        check(has(c, "getInstruction"), "method getInstruction");
        check(has(c, "setInstruction"), "method setInstruction");
        check(has(c, "swapInstruction"), "method swapInstruction");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "removeAllTargeters"), "method removeAllTargeters");
        check(has(c, "removeTargeter"), "method removeTargeter");
        check(has(c, "addTargeter"), "method addTargeter");
        check(has(c, "hasTargeters"), "method hasTargeters");
        check(has(c, "getTargeters"), "method getTargeters");
        check(has(c, "toString"), "method toString");
        check(has(c, "addAttribute"), "method addAttribute");
        check(has(c, "removeAttribute"), "method removeAttribute");
        check(has(c, "getAttribute"), "method getAttribute");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
