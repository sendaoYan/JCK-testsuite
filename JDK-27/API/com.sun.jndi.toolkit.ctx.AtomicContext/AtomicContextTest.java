/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.AtomicContext conformance: internal abstract class reflection.
 * @modules java.naming/com.sun.jndi.toolkit.ctx:+open
 * @run main AtomicContextTest
 */
public class AtomicContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.AtomicContext", false, AtomicContextTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.AtomicContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.ComponentContext"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("a_bind;a_bind_nns;a_createSubcontext;a_createSubcontext_nns;a_destroySubcontext;a_destroySubcontext_nns;a_getNameParser;a_getNameParser_nns;a_list;a_listBindings;a_listBindings_nns;a_list_nns;a_lookup;a_lookupLink;a_lookupLink_nns;a_lookup_nns;a_processJunction_nns;a_rebind;a_rebind_nns;a_rename;a_rename_nns;a_resolveIntermediate_nns;a_unbind;a_unbind_nns;c_bind;c_bind_nns;c_createSubcontext;c_createSubcontext_nns;c_destroySubcontext;c_destroySubcontext_nns;c_getNameParser;c_getNameParser_nns;c_list;c_listBindings;c_listBindings_nns;c_list_nns;c_lookup;c_lookupLink;c_lookupLink_nns;c_lookup_nns;c_parseComponent;c_rebind;c_rebind_nns;c_rename;c_rename_nns;c_resolveIntermediate_nns;c_unbind;c_unbind_nns;isEmpty;resolve_to_context;resolve_to_nns_and_continue;resolve_to_penultimate_context;resolve_to_penultimate_context_nns"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
