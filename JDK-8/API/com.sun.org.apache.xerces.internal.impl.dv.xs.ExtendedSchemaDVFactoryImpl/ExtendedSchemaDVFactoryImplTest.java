/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.ExtendedSchemaDVFactoryImpl conformance: class hierarchy and members.
 * @run main ExtendedSchemaDVFactoryImplTest
 */
public class ExtendedSchemaDVFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.ExtendedSchemaDVFactoryImpl", false, ExtendedSchemaDVFactoryImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dv.xs.BaseSchemaDVFactory"), "superclass com.sun.org.apache.xerces.internal.impl.dv.xs.BaseSchemaDVFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBuiltInTypes"), "method createBuiltInTypes");
        check(mn.contains("getBuiltInType"), "method getBuiltInType");
        check(mn.contains("getBuiltInTypes"), "method getBuiltInTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
