/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FieldInstruction conformance: reflection.
 * @run main FieldInstructionTest
 */

public class FieldInstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FieldInstruction", false, FieldInstructionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FieldInstruction"), "simple name FieldInstruction");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldOrMethod"), "superclass com.sun.org.apache.bcel.internal.generic.FieldOrMethod");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.TypedInstruction"), "interface com.sun.org.apache.bcel.internal.generic.TypedInstruction");
        check(has(c, "toString"), "method toString");
        check(has(c, "getType"), "method getType");
        check(has(c, "getFieldType"), "method getFieldType");
        check(has(c, "getFieldName"), "method getFieldName");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
