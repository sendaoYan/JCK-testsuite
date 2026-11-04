/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.StringValueHandler conformance: reflection.
 * @run main StringValueHandlerTest
 */

public class StringValueHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.StringValueHandler", false, StringValueHandlerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StringValueHandler"), "simple name StringValueHandler");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.EmptySerializer"), "superclass com.sun.org.apache.xml.internal.serializer.EmptySerializer");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "characters"), "method characters");
        check(has(c, "getValue"), "method getValue");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "setEscaping"), "method setEscaping");
        check(has(c, "getValueOfPI"), "method getValueOfPI");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
