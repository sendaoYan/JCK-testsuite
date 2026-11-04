/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.VariableStack conformance: reflection.
 * @run main VariableStackTest
 */

public class VariableStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.VariableStack", false, VariableStackTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("VariableStack"), "simple name VariableStack");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "clone"), "method clone");
        check(has(c, "elementAt"), "method elementAt");
        check(has(c, "size"), "method size");
        check(has(c, "reset"), "method reset");
        check(has(c, "setStackFrame"), "method setStackFrame");
        check(has(c, "getStackFrame"), "method getStackFrame");
        check(has(c, "link"), "method link");
        check(has(c, "unlink"), "method unlink");
        check(has(c, "setLocalVariable"), "method setLocalVariable");
        check(has(c, "getLocalVariable"), "method getLocalVariable");
        check(has(c, "isLocalSet"), "method isLocalSet");
        check(has(c, "clearLocalSlots"), "method clearLocalSlots");
        check(has(c, "setGlobalVariable"), "method setGlobalVariable");
        check(has(c, "getGlobalVariable"), "method getGlobalVariable");
        check(has(c, "getVariableOrParam"), "method getVariableOrParam");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
