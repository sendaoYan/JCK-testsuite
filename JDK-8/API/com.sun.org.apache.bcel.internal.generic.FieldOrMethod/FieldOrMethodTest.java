/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FieldOrMethod conformance: reflection.
 * @run main FieldOrMethodTest
 */

public class FieldOrMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FieldOrMethod", false, FieldOrMethodTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FieldOrMethod"), "simple name FieldOrMethod");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.CPInstruction"), "superclass com.sun.org.apache.bcel.internal.generic.CPInstruction");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.LoadClass"), "interface com.sun.org.apache.bcel.internal.generic.LoadClass");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "getName"), "method getName");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "getClassType"), "method getClassType");
        check(has(c, "getLoadClassType"), "method getLoadClassType");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
