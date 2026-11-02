/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Template conformance: reflection.
 * @run main TemplateTest
 */

public class TemplateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Template", false, TemplateTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Template"), "simple name Template");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.TopLevelElement");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "hasParams"), "method hasParams");
        check(has(c, "isSimplified"), "method isSimplified");
        check(has(c, "setSimplified"), "method setSimplified");
        check(has(c, "isSimpleNamedTemplate"), "method isSimpleNamedTemplate");
        check(has(c, "addParameter"), "method addParameter");
        check(has(c, "getParameters"), "method getParameters");
        check(has(c, "disable"), "method disable");
        check(has(c, "disabled"), "method disabled");
        check(has(c, "getPriority"), "method getPriority");
        check(has(c, "getPosition"), "method getPosition");
        check(has(c, "isNamed"), "method isNamed");
        check(has(c, "getPattern"), "method getPattern");
        check(has(c, "getName"), "method getName");
        check(has(c, "setName"), "method setName");
        check(has(c, "getModeName"), "method getModeName");
        check(has(c, "compareTo"), "method compareTo");
        check(has(c, "display"), "method display");
        check(has(c, "getStylesheet"), "method getStylesheet");
        check(has(c, "parseContents"), "method parseContents");
        check(has(c, "parseSimplified"), "method parseSimplified");
        check(has(c, "typeCheck"), "method typeCheck");
        check(has(c, "translate"), "method translate");
        check(has(c, "getDependencies"), "method getDependencies");
        check(has(c, "addDependency"), "method addDependency");
        check(has(c, "compile"), "method compile");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
