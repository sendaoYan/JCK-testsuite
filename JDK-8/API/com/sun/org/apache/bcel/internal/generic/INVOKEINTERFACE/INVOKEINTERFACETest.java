/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE conformance: reflection.
 * @run main INVOKEINTERFACETest
 */

public class INVOKEINTERFACETest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.INVOKEINTERFACE", false, INVOKEINTERFACETest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("INVOKEINTERFACE"), "simple name INVOKEINTERFACE");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.InvokeInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.InvokeInstruction");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "dump"), "method dump");
        check(has(c, "getCount"), "method getCount");
        check(has(c, "toString"), "method toString");
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
