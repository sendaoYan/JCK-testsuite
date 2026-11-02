/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.jaxp.JAXPVariableStack conformance: reflection.
 * @run main JAXPVariableStackTest
 */

public class JAXPVariableStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.jaxp.JAXPVariableStack", false, JAXPVariableStackTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("JAXPVariableStack"), "simple name JAXPVariableStack");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.VariableStack"), "superclass com.sun.org.apache.xpath.internal.VariableStack");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getVariableOrParam"), "method getVariableOrParam");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
