/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.CHECKCAST conformance: reflection.
 * @run main CHECKCASTTest
 */

public class CHECKCASTTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.CHECKCAST", false, CHECKCASTTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CHECKCAST"), "simple name CHECKCAST");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.CPInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.CPInstruction");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.LoadClass"), "interface com.sun.org.apache.bcel.internal.generic.LoadClass");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.ExceptionThrower"), "interface com.sun.org.apache.bcel.internal.generic.ExceptionThrower");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackProducer"), "interface com.sun.org.apache.bcel.internal.generic.StackProducer");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackConsumer"), "interface com.sun.org.apache.bcel.internal.generic.StackConsumer");
        check(has(c, "getExceptions"), "method getExceptions");
        check(has(c, "getLoadClassType"), "method getLoadClassType");
        check(has(c, "accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
