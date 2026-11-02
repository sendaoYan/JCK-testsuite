/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.CodeExceptionGen conformance: reflection.
 * @run main CodeExceptionGenTest
 */

public class CodeExceptionGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.CodeExceptionGen", false, CodeExceptionGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("CodeExceptionGen"), "simple name CodeExceptionGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 3, "interfaces count 3");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionTargeter"), "interface com.sun.org.apache.bcel.internal.generic.InstructionTargeter");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "getCodeException"), "method getCodeException");
        check(has(c, "setStartPC"), "method setStartPC");
        check(has(c, "setEndPC"), "method setEndPC");
        check(has(c, "setHandlerPC"), "method setHandlerPC");
        check(has(c, "updateTarget"), "method updateTarget");
        check(has(c, "containsTarget"), "method containsTarget");
        check(has(c, "setCatchType"), "method setCatchType");
        check(has(c, "getCatchType"), "method getCatchType");
        check(has(c, "getStartPC"), "method getStartPC");
        check(has(c, "getEndPC"), "method getEndPC");
        check(has(c, "getHandlerPC"), "method getHandlerPC");
        check(has(c, "toString"), "method toString");
        check(has(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
