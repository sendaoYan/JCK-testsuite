/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.PartialCompositeContext conformance: hierarchy and declared members via reflection.
 * @run main PartialCompositeContextTest
 */

public class PartialCompositeContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.PartialCompositeContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.Context"), "implements javax.naming.Context");
        check(in.contains("javax.naming.spi.Resolver"), "implements javax.naming.spi.Resolver");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("allEmpty"), "declares allEmpty");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("destroySubcontext"), "declares destroySubcontext");
        check(ms.contains("getNameParser"), "declares getNameParser");
        check(ms.contains("getPCContext"), "declares getPCContext");
        check(ms.contains("list"), "declares list");
        check(ms.contains("listBindings"), "declares listBindings");
        check(ms.contains("lookup"), "declares lookup");
        check(ms.contains("lookupLink"), "declares lookupLink");
        check(ms.contains("p_bind"), "declares p_bind");
        check(ms.contains("p_createSubcontext"), "declares p_createSubcontext");
        check(ms.contains("p_destroySubcontext"), "declares p_destroySubcontext");
        check(ms.contains("p_getEnvironment"), "declares p_getEnvironment");
        check(ms.contains("p_getNameParser"), "declares p_getNameParser");
        check(ms.contains("p_list"), "declares p_list");
        check(ms.contains("p_listBindings"), "declares p_listBindings");
        check(ms.contains("p_lookup"), "declares p_lookup");
        check(ms.contains("p_lookupLink"), "declares p_lookupLink");
        check(ms.contains("p_rebind"), "declares p_rebind");
        check(ms.contains("p_rename"), "declares p_rename");
        check(ms.contains("p_resolveToClass"), "declares p_resolveToClass");
        check(ms.contains("p_unbind"), "declares p_unbind");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("rename"), "declares rename");
        check(ms.contains("resolveToClass"), "declares resolveToClass");
        check(ms.contains("unbind"), "declares unbind");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
