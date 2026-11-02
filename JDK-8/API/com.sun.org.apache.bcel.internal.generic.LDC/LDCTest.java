/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.LDC conformance: reflection.
 * @run main LDCTest
 */

public class LDCTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.LDC", false, LDCTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LDC"), "simple name LDC");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.CPInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.CPInstruction");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.PushInstruction"), "interface com.sun.org.apache.bcel.internal.generic.PushInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.ExceptionThrower"), "interface com.sun.org.apache.bcel.internal.generic.ExceptionThrower");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(has(c, "dump"), "method dump");
        check(has(c, "setIndex"), "method setIndex");
        check(has(c, "getValue"), "method getValue");
        check(has(c, "getType"), "method getType");
        check(has(c, "getExceptions"), "method getExceptions");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
