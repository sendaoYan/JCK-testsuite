/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.ComponentContext conformance: internal abstract class reflection.
 * @modules java.naming/com.sun.jndi.toolkit.ctx:+open
 * @run main ComponentContextTest
 */
public class ComponentContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.ComponentContext", false, ComponentContextTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.ComponentContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeContext"), "superclass");
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
            check(String.join(";", s).equals("c_bind;c_bind_nns;c_createSubcontext;c_createSubcontext_nns;c_destroySubcontext;c_destroySubcontext_nns;c_getNameParser;c_getNameParser_nns;c_list;c_listBindings;c_listBindings_nns;c_list_nns;c_lookup;c_lookupLink;c_lookupLink_nns;c_lookup_nns;c_processJunction_nns;c_rebind;c_rebind_nns;c_rename;c_rename_nns;c_resolveIntermediate_nns;c_unbind;c_unbind_nns;isAllEmpty;p_bind;p_createSubcontext;p_destroySubcontext;p_getNameParser;p_list;p_listBindings;p_lookup;p_lookupLink;p_parseComponent;p_rebind;p_rename;p_resolveIntermediate;p_resolveToClass;p_unbind"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("TERMINAL_COMPONENT;TERMINAL_NNS_COMPONENT;USE_CONTINUATION"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
