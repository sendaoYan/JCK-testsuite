/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator.ValueStoreBase conformance: abstract class hierarchy and members.
 * @run main ValueStoreBaseTest
 */
public class ValueStoreBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator$ValueStoreBase", false, ValueStoreBaseTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.xs.identity.ValueStore"), "has interface com.sun.org.apache.xerces.internal.impl.xs.identity.ValueStore");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clear"), "method clear");
        check(mn.contains("append"), "method append");
        check(mn.contains("startValueScope"), "method startValueScope");
        check(mn.contains("endValueScope"), "method endValueScope");
        check(mn.contains("endDocumentFragment"), "method endDocumentFragment");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("reportError"), "method reportError");
        check(mn.contains("addValue"), "method addValue");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("checkDuplicateValues"), "method checkDuplicateValues");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
