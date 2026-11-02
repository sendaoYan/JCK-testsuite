/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType conformance: reflection.
 * @run main IntTypeTest
 */

public class IntTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType", false, IntTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("IntType"), "simple name IntType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "toString"), "method toString");
        check(has(c, "identicalTo"), "method identicalTo");
        check(has(c, "toSignature"), "method toSignature");
        check(has(c, "toJCType"), "method toJCType");
        check(has(c, "distanceTo"), "method distanceTo");
        check(has(c, "translateTo"), "method translateTo");
        check(has(c, "translateToDesynthesized"), "method translateToDesynthesized");
        check(has(c, "translateBox"), "method translateBox");
        check(has(c, "translateUnBox"), "method translateUnBox");
        check(has(c, "ADD"), "method ADD");
        check(has(c, "SUB"), "method SUB");
        check(has(c, "MUL"), "method MUL");
        check(has(c, "DIV"), "method DIV");
        check(has(c, "REM"), "method REM");
        check(has(c, "NEG"), "method NEG");
        check(has(c, "LOAD"), "method LOAD");
        check(has(c, "STORE"), "method STORE");
        check(has(c, "GT"), "method GT");
        check(has(c, "GE"), "method GE");
        check(has(c, "LT"), "method LT");
        check(has(c, "LE"), "method LE");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
