/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType conformance: reflection.
 * @run main ResultTreeTypeTest
 */

public class ResultTreeTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType", false, ResultTreeTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ResultTreeType"), "simple name ResultTreeType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "toString"), "method toString");
        check(has(c, "identicalTo"), "method identicalTo");
        check(has(c, "toSignature"), "method toSignature");
        check(has(c, "toJCType"), "method toJCType");
        check(has(c, "getMethodName"), "method getMethodName");
        check(has(c, "implementedAsMethod"), "method implementedAsMethod");
        check(has(c, "translateTo"), "method translateTo");
        check(has(c, "translateToDesynthesized"), "method translateToDesynthesized");
        check(has(c, "translateBox"), "method translateBox");
        check(has(c, "translateUnBox"), "method translateUnBox");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "LOAD"), "method LOAD");
        check(has(c, "STORE"), "method STORE");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
