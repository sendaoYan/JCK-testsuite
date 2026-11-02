/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InvokeInstruction conformance: reflection.
 * @run main InvokeInstructionTest
 */

public class InvokeInstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InvokeInstruction", false, InvokeInstructionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("InvokeInstruction"), "simple name InvokeInstruction");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldOrMethod"), "superclass com.sun.org.apache.bcel.internal.generic.FieldOrMethod");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.ExceptionThrower"), "interface com.sun.org.apache.bcel.internal.generic.ExceptionThrower");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackConsumer"), "interface com.sun.org.apache.bcel.internal.generic.StackConsumer");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.StackProducer"), "interface com.sun.org.apache.bcel.internal.generic.StackProducer");
        check(has(c, "toString"), "method toString");
        check(has(c, "consumeStack"), "method consumeStack");
        check(has(c, "produceStack"), "method produceStack");
        check(has(c, "getType"), "method getType");
        check(has(c, "getMethodName"), "method getMethodName");
        check(has(c, "getReturnType"), "method getReturnType");
        check(has(c, "getArgumentTypes"), "method getArgumentTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
