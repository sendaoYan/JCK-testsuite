/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.BaseSchemaDVFactory conformance: abstract class hierarchy and members.
 * @run main BaseSchemaDVFactoryTest
 */
public class BaseSchemaDVFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.BaseSchemaDVFactory", false, BaseSchemaDVFactoryTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory"), "superclass com.sun.org.apache.xerces.internal.impl.dv.SchemaDVFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBuiltInTypes"), "method createBuiltInTypes");
        check(mn.contains("createTypeRestriction"), "method createTypeRestriction");
        check(mn.contains("createTypeList"), "method createTypeList");
        check(mn.contains("createTypeUnion"), "method createTypeUnion");
        check(mn.contains("setDeclPool"), "method setDeclPool");
        check(mn.contains("newXSSimpleTypeDecl"), "method newXSSimpleTypeDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
