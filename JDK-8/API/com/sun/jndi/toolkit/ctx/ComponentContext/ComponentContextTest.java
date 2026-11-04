/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.ComponentContext conformance: hierarchy and declared members via reflection.
 * @run main ComponentContextTest
 */

public class ComponentContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.ComponentContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.ComponentContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeContext"), "superclass com.sun.jndi.toolkit.ctx.PartialCompositeContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("c_bind"), "declares c_bind");
        check(ms.contains("c_bind_nns"), "declares c_bind_nns");
        check(ms.contains("c_createSubcontext"), "declares c_createSubcontext");
        check(ms.contains("c_createSubcontext_nns"), "declares c_createSubcontext_nns");
        check(ms.contains("c_destroySubcontext"), "declares c_destroySubcontext");
        check(ms.contains("c_destroySubcontext_nns"), "declares c_destroySubcontext_nns");
        check(ms.contains("c_getNameParser"), "declares c_getNameParser");
        check(ms.contains("c_getNameParser_nns"), "declares c_getNameParser_nns");
        check(ms.contains("c_list"), "declares c_list");
        check(ms.contains("c_listBindings"), "declares c_listBindings");
        check(ms.contains("c_listBindings_nns"), "declares c_listBindings_nns");
        check(ms.contains("c_list_nns"), "declares c_list_nns");
        check(ms.contains("c_lookup"), "declares c_lookup");
        check(ms.contains("c_lookupLink"), "declares c_lookupLink");
        check(ms.contains("c_lookupLink_nns"), "declares c_lookupLink_nns");
        check(ms.contains("c_lookup_nns"), "declares c_lookup_nns");
        check(ms.contains("c_processJunction_nns"), "declares c_processJunction_nns");
        check(ms.contains("c_rebind"), "declares c_rebind");
        check(ms.contains("c_rebind_nns"), "declares c_rebind_nns");
        check(ms.contains("c_rename"), "declares c_rename");
        check(ms.contains("c_rename_nns"), "declares c_rename_nns");
        check(ms.contains("c_resolveIntermediate_nns"), "declares c_resolveIntermediate_nns");
        check(ms.contains("c_unbind"), "declares c_unbind");
        check(ms.contains("c_unbind_nns"), "declares c_unbind_nns");
        check(ms.contains("checkAndAdjustRemainingName"), "declares checkAndAdjustRemainingName");
        check(ms.contains("isAllEmpty"), "declares isAllEmpty");
        check(ms.contains("p_bind"), "declares p_bind");
        check(ms.contains("p_createSubcontext"), "declares p_createSubcontext");
        check(ms.contains("p_destroySubcontext"), "declares p_destroySubcontext");
        check(ms.contains("p_getNameParser"), "declares p_getNameParser");
        check(ms.contains("p_list"), "declares p_list");
        check(ms.contains("p_listBindings"), "declares p_listBindings");
        check(ms.contains("p_lookup"), "declares p_lookup");
        check(ms.contains("p_lookupLink"), "declares p_lookupLink");
        check(ms.contains("p_parseComponent"), "declares p_parseComponent");
        check(ms.contains("p_rebind"), "declares p_rebind");
        check(ms.contains("p_rename"), "declares p_rename");
        check(ms.contains("p_resolveIntermediate"), "declares p_resolveIntermediate");
        check(ms.contains("p_resolveToClass"), "declares p_resolveToClass");
        check(ms.contains("p_unbind"), "declares p_unbind");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
