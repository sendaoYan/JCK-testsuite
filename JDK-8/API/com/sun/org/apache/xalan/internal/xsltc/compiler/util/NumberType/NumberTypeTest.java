/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType conformance: reflection.
 * @run main NumberTypeTest
 */

public class NumberTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType", false, NumberTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("NumberType"), "simple name NumberType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type"), "superclass com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "isNumber"), "method isNumber");
        check(has(c, "isSimple"), "method isSimple");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
