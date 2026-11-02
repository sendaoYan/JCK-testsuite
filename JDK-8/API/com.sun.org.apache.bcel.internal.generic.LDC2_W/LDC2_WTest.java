/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.LDC2_W conformance: reflection.
 * @run main LDC2_WTest
 */

public class LDC2_WTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.LDC2_W", false, LDC2_WTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LDC2_W"), "simple name LDC2_W");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.CPInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.CPInstruction");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.PushInstruction"), "interface com.sun.org.apache.bcel.internal.generic.PushInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(has(c, "getType"), "method getType");
        check(has(c, "getValue"), "method getValue");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
