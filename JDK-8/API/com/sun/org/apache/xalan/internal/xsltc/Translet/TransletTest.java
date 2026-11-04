/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.Translet conformance: reflection.
 * @run main TransletTest
 */

public class TransletTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.Translet", false, TransletTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Translet"), "simple name Translet");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "transform"), "method transform");
        check(has(c, "addParameter"), "method addParameter");
        check(has(c, "buildKeys"), "method buildKeys");
        check(has(c, "addAuxiliaryClass"), "method addAuxiliaryClass");
        check(has(c, "getAuxiliaryClass"), "method getAuxiliaryClass");
        check(has(c, "getNamesArray"), "method getNamesArray");
        check(has(c, "getUrisArray"), "method getUrisArray");
        check(has(c, "getTypesArray"), "method getTypesArray");
        check(has(c, "getNamespaceArray"), "method getNamespaceArray");
        check(has(c, "overrideDefaultParser"), "method overrideDefaultParser");
        check(has(c, "setOverrideDefaultParser"), "method setOverrideDefaultParser");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
