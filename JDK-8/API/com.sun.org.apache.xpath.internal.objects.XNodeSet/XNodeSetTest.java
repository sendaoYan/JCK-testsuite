/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XNodeSet conformance: reflection.
 * @run main XNodeSetTest
 */

public class XNodeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XNodeSet", false, XNodeSetTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XNodeSet"), "simple name XNodeSet");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.axes.NodeSequence"), "superclass com.sun.org.apache.xpath.internal.axes.NodeSequence");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getType"), "method getType");
        check(has(c, "getTypeString"), "method getTypeString");
        check(has(c, "getNumberFromNode"), "method getNumberFromNode");
        check(has(c, "num"), "method num");
        check(has(c, "numWithSideEffects"), "method numWithSideEffects");
        check(has(c, "bool"), "method bool");
        check(has(c, "boolWithSideEffects"), "method boolWithSideEffects");
        check(has(c, "getStringFromNode"), "method getStringFromNode");
        check(has(c, "dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(has(c, "xstr"), "method xstr");
        check(has(c, "appendToFsb"), "method appendToFsb");
        check(has(c, "str"), "method str");
        check(has(c, "object"), "method object");
        check(has(c, "nodeset"), "method nodeset");
        check(has(c, "nodelist"), "method nodelist");
        check(has(c, "iterRaw"), "method iterRaw");
        check(has(c, "release"), "method release");
        check(has(c, "iter"), "method iter");
        check(has(c, "getFresh"), "method getFresh");
        check(has(c, "mutableNodeset"), "method mutableNodeset");
        check(has(c, "compare"), "method compare");
        check(has(c, "lessThan"), "method lessThan");
        check(has(c, "lessThanOrEqual"), "method lessThanOrEqual");
        check(has(c, "greaterThan"), "method greaterThan");
        check(has(c, "greaterThanOrEqual"), "method greaterThanOrEqual");
        check(has(c, "equals"), "method equals");
        check(has(c, "notEquals"), "method notEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
