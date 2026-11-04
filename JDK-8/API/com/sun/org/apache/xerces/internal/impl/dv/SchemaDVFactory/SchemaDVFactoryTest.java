/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory conformance: abstract class hierarchy and members.
 * @run main SchemaDVFactoryTest
 */
public class SchemaDVFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory", false, SchemaDVFactoryTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getBuiltInType"), "method getBuiltInType");
        check(mn.contains("getBuiltInTypes"), "method getBuiltInTypes");
        check(mn.contains("createTypeRestriction"), "method createTypeRestriction");
        check(mn.contains("createTypeList"), "method createTypeList");
        check(mn.contains("createTypeUnion"), "method createTypeUnion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
