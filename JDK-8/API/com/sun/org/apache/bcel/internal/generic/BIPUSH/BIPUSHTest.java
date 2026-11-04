/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.BIPUSH conformance: reflection.
 * @run main BIPUSHTest
 */

public class BIPUSHTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.BIPUSH", false, BIPUSHTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BIPUSH"), "simple name BIPUSH");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Instruction"), "superclass com.sun.org.apache.bcel.internal.generic.Instruction");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction"), "interface com.sun.org.apache.bcel.internal.generic.ConstantPushInstruction");
        check(has(c, "dump"), "method dump");
        check(has(c, "toString"), "method toString");
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
