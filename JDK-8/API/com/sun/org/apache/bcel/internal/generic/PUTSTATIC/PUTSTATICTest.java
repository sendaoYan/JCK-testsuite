/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.PUTSTATIC conformance: reflection.
 * @run main PUTSTATICTest
 */

public class PUTSTATICTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.PUTSTATIC", false, PUTSTATICTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("PUTSTATIC"), "simple name PUTSTATIC");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.FieldInstruction");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.ExceptionThrower"), "interface com.sun.org.apache.bcel.internal.generic.ExceptionThrower");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.PopInstruction"), "interface com.sun.org.apache.bcel.internal.generic.PopInstruction");
        check(has(c, "consumeStack"), "method consumeStack");
        check(has(c, "getExceptions"), "method getExceptions");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
