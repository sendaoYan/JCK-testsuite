/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.PartialCompositeDirContext conformance: internal abstract class reflection.
 * @modules java.naming/com.sun.jndi.toolkit.ctx:+open
 * @run main PartialCompositeDirContextTest
 */
public class PartialCompositeDirContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.PartialCompositeDirContext", false, PartialCompositeDirContextTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeDirContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.AtomicContext"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("javax.naming.directory.DirContext"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("a_bind;a_createSubcontext;a_destroySubcontext;a_getNameParser;a_list;a_listBindings;a_lookup;a_lookupLink;a_rebind;a_rename;a_unbind;bind;c_parseComponent;createSubcontext;getAttributes;getPCDirContext;getSchema;getSchemaClassDefinition;modifyAttributes;p_bind;p_createSubcontext;p_getAttributes;p_getSchema;p_getSchemaClassDefinition;p_modifyAttributes;p_rebind;p_search;rebind;search"), "methods");
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
