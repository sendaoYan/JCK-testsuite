/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.PartialCompositeContext conformance: internal abstract class reflection.
 * @modules java.naming/com.sun.jndi.toolkit.ctx:+open
 * @run main PartialCompositeContextTest
 */
public class PartialCompositeContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.PartialCompositeContext", false, PartialCompositeContextTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("javax.naming.Context;javax.naming.spi.Resolver"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("allEmpty;bind;composeName;createSubcontext;destroySubcontext;getNameParser;getPCContext;list;listBindings;lookup;lookupLink;p_bind;p_createSubcontext;p_destroySubcontext;p_getEnvironment;p_getNameParser;p_list;p_listBindings;p_lookup;p_lookupLink;p_rebind;p_rename;p_resolveToClass;p_unbind;rebind;rename;resolveToClass;unbind"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("_ATOMIC;_COMPONENT;_PARTIAL;_contextType"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
