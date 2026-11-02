/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.LineNumberGen conformance: reflection.
 * @run main LineNumberGenTest
 */

public class LineNumberGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.LineNumberGen", false, LineNumberGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LineNumberGen"), "simple name LineNumberGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionTargeter"), "interface com.sun.org.apache.bcel.internal.generic.InstructionTargeter");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "containsTarget"), "method containsTarget");
        check(has(c, "updateTarget"), "method updateTarget");
        check(has(c, "getLineNumber"), "method getLineNumber");
        check(has(c, "setInstruction"), "method setInstruction");
        check(has(c, "clone"), "method clone");
        check(has(c, "getInstruction"), "method getInstruction");
        check(has(c, "setSourceLine"), "method setSourceLine");
        check(has(c, "getSourceLine"), "method getSourceLine");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
