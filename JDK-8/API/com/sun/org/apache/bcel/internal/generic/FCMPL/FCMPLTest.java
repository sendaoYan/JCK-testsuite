/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FCMPL conformance: reflection.
 * @run main FCMPLTest
 */

public class FCMPLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FCMPL", false, FCMPLTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FCMPL"), "simple name FCMPL");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Instruction"), "superclass com.sun.org.apache.bcel.internal.generic.Instruction");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackProducer"), "interface com.sun.org.apache.bcel.internal.generic.StackProducer");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackConsumer"), "interface com.sun.org.apache.bcel.internal.generic.StackConsumer");
        check(has(c, "getType"), "method getType");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
