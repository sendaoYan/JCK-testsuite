/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.operations.Variable conformance: reflection.
 * @run main VariableTest
 */

public class VariableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.operations.Variable", false, VariableTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Variable"), "simple name Variable");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.Expression"), "superclass com.sun.org.apache.xpath.internal.Expression");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xpath.internal.axes.PathComponent"), "interface com.sun.org.apache.xpath.internal.axes.PathComponent");
        check(has(c, "setIndex"), "method setIndex");
        check(has(c, "getIndex"), "method getIndex");
        check(has(c, "setIsGlobal"), "method setIsGlobal");
        check(has(c, "getGlobal"), "method getGlobal");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "setQName"), "method setQName");
        check(has(c, "getQName"), "method getQName");
        check(has(c, "execute"), "method execute");
        check(has(c, "isStableNumber"), "method isStableNumber");
        check(has(c, "getAnalysisBits"), "method getAnalysisBits");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        check(has(c, "isPsuedoVarRef"), "method isPsuedoVarRef");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
