/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.LocalVariableGen conformance: reflection.
 * @run main LocalVariableGenTest
 */

public class LocalVariableGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.LocalVariableGen", false, LocalVariableGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LocalVariableGen"), "simple name LocalVariableGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionTargeter"), "interface com.sun.org.apache.bcel.internal.generic.InstructionTargeter");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.NamedAndTyped"), "interface com.sun.org.apache.bcel.internal.generic.NamedAndTyped");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "getLocalVariable"), "method getLocalVariable");
        check(has(c, "getIndex"), "method getIndex");
        check(has(c, "setName"), "method setName");
        check(has(c, "getName"), "method getName");
        check(has(c, "setType"), "method setType");
        check(has(c, "getType"), "method getType");
        check(has(c, "getStart"), "method getStart");
        check(has(c, "getEnd"), "method getEnd");
        check(has(c, "setStart"), "method setStart");
        check(has(c, "setEnd"), "method setEnd");
        check(has(c, "updateTarget"), "method updateTarget");
        check(has(c, "containsTarget"), "method containsTarget");
        check(has(c, "equals"), "method equals");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "toString"), "method toString");
        check(has(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
