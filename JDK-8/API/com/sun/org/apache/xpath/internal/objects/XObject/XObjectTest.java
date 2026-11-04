/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XObject conformance: reflection.
 * @run main XObjectTest
 */

public class XObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XObject", false, XObjectTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XObject"), "simple name XObject");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xpath.internal.Expression"), "superclass com.sun.org.apache.xpath.internal.Expression");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "execute"), "method execute");
        check(has(c, "allowDetachToRelease"), "method allowDetachToRelease");
        check(has(c, "detach"), "method detach");
        check(has(c, "destruct"), "method destruct");
        check(has(c, "reset"), "method reset");
        check(has(c, "dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(has(c, "create"), "method create");
        check(has(c, "getType"), "method getType");
        check(has(c, "getTypeString"), "method getTypeString");
        check(has(c, "num"), "method num");
        check(has(c, "numWithSideEffects"), "method numWithSideEffects");
        check(has(c, "bool"), "method bool");
        check(has(c, "boolWithSideEffects"), "method boolWithSideEffects");
        check(has(c, "xstr"), "method xstr");
        check(has(c, "str"), "method str");
        check(has(c, "toString"), "method toString");
        check(has(c, "rtf"), "method rtf");
        check(has(c, "rtree"), "method rtree");
        check(has(c, "object"), "method object");
        check(has(c, "iter"), "method iter");
        check(has(c, "getFresh"), "method getFresh");
        check(has(c, "nodeset"), "method nodeset");
        check(has(c, "nodelist"), "method nodelist");
        check(has(c, "mutableNodeset"), "method mutableNodeset");
        check(has(c, "castToType"), "method castToType");
        check(has(c, "lessThan"), "method lessThan");
        check(has(c, "lessThanOrEqual"), "method lessThanOrEqual");
        check(has(c, "greaterThan"), "method greaterThan");
        check(has(c, "greaterThanOrEqual"), "method greaterThanOrEqual");
        check(has(c, "equals"), "method equals");
        check(has(c, "notEquals"), "method notEquals");
        check(has(c, "fixupVariables"), "method fixupVariables");
        check(has(c, "appendToFsb"), "method appendToFsb");
        check(has(c, "callVisitors"), "method callVisitors");
        check(has(c, "deepEquals"), "method deepEquals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
