/*
 * @test
 * @summary com.sun.jndi.ldap.LdapCtx conformance: internal class reflection.
 * @modules java.naming/com.sun.jndi.ldap:+open
 * @run main LdapCtxTest
 */
public class LdapCtxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapCtx", false, LdapCtxTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.jndi.ldap.LdapCtx"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.ComponentDirContext"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("javax.naming.event.EventDirContext;javax.naming.ldap.LdapContext"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("addNamingListener;addToEnvironment;c_bind;c_createSubcontext;c_destroySubcontext;c_getAttributes;c_getNameParser;c_getSchema;c_getSchemaClassDefinition;c_list;c_listBindings;c_lookup;c_lookupLink;c_modifyAttributes;c_rebind;c_rename;c_search;c_unbind;close;composeName;extendedOperation;finalize;getConnectControls;getEnvironment;getNameInNamespace;getRequestControls;getResponseControls;mapErrorCode;newInstance;p_getEnvironment;processReturnCode;reconnect;removeFromEnvironment;removeNamingListener;setRequestControls;targetMustExist;trustSerialData"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("DEFAULT_HOST;DEFAULT_PORT;DEFAULT_SSL_PORT"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
