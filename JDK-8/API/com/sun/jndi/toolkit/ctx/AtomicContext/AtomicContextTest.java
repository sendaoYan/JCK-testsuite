/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.AtomicContext conformance: hierarchy and declared members via reflection.
 * @run main AtomicContextTest
 */

public class AtomicContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.AtomicContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.AtomicContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.ComponentContext"), "superclass com.sun.jndi.toolkit.ctx.ComponentContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("a_bind"), "declares a_bind");
        check(ms.contains("a_bind_nns"), "declares a_bind_nns");
        check(ms.contains("a_createSubcontext"), "declares a_createSubcontext");
        check(ms.contains("a_createSubcontext_nns"), "declares a_createSubcontext_nns");
        check(ms.contains("a_destroySubcontext"), "declares a_destroySubcontext");
        check(ms.contains("a_destroySubcontext_nns"), "declares a_destroySubcontext_nns");
        check(ms.contains("a_getNameParser"), "declares a_getNameParser");
        check(ms.contains("a_getNameParser_nns"), "declares a_getNameParser_nns");
        check(ms.contains("a_list"), "declares a_list");
        check(ms.contains("a_listBindings"), "declares a_listBindings");
        check(ms.contains("a_listBindings_nns"), "declares a_listBindings_nns");
        check(ms.contains("a_list_nns"), "declares a_list_nns");
        check(ms.contains("a_lookup"), "declares a_lookup");
        check(ms.contains("a_lookupLink"), "declares a_lookupLink");
        check(ms.contains("a_lookupLink_nns"), "declares a_lookupLink_nns");
        check(ms.contains("a_lookup_nns"), "declares a_lookup_nns");
        check(ms.contains("a_processJunction_nns"), "declares a_processJunction_nns");
        check(ms.contains("a_rebind"), "declares a_rebind");
        check(ms.contains("a_rebind_nns"), "declares a_rebind_nns");
        check(ms.contains("a_rename"), "declares a_rename");
        check(ms.contains("a_rename_nns"), "declares a_rename_nns");
        check(ms.contains("a_resolveIntermediate_nns"), "declares a_resolveIntermediate_nns");
        check(ms.contains("a_unbind"), "declares a_unbind");
        check(ms.contains("a_unbind_nns"), "declares a_unbind_nns");
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
        check(ms.contains("c_parseComponent"), "declares c_parseComponent");
        check(ms.contains("c_rebind"), "declares c_rebind");
        check(ms.contains("c_rebind_nns"), "declares c_rebind_nns");
        check(ms.contains("c_rename"), "declares c_rename");
        check(ms.contains("c_rename_nns"), "declares c_rename_nns");
        check(ms.contains("c_resolveIntermediate_nns"), "declares c_resolveIntermediate_nns");
        check(ms.contains("c_unbind"), "declares c_unbind");
        check(ms.contains("c_unbind_nns"), "declares c_unbind_nns");
        check(ms.contains("isEmpty"), "declares isEmpty");
        check(ms.contains("resolve_to_context"), "declares resolve_to_context");
        check(ms.contains("resolve_to_nns_and_continue"), "declares resolve_to_nns_and_continue");
        check(ms.contains("resolve_to_penultimate_context"), "declares resolve_to_penultimate_context");
        check(ms.contains("resolve_to_penultimate_context_nns"), "declares resolve_to_penultimate_context_nns");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
