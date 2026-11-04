/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator conformance: reflection.
 * @run main ClassGeneratorTest
 */

public class ClassGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.ClassGenerator", false, ClassGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ClassGenerator"), "simple name ClassGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.ClassGen"), "superclass com.sun.org.apache.bcel.internal.generic.ClassGen");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getParser"), "method getParser");
        check(has(c, "getStylesheet"), "method getStylesheet");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "loadTranslet"), "method loadTranslet");
        check(has(c, "getDOMClass"), "method getDOMClass");
        check(has(c, "getDOMClassSig"), "method getDOMClassSig");
        check(has(c, "getApplyTemplatesSig"), "method getApplyTemplatesSig");
        check(has(c, "getApplyTemplatesSigForImport"), "method getApplyTemplatesSigForImport");
        check(has(c, "isExternal"), "method isExternal");
        check(has(c, "addMethod"), "method addMethod");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
