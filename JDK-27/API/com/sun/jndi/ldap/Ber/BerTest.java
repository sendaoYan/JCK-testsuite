/*
 * @test
 * @summary com.sun.jndi.ldap.Ber conformance: internal abstract class reflection.
 * @modules java.naming/com.sun.jndi.ldap:+open
 * @run main BerTest
 */
public class BerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.Ber", false, BerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.jndi.ldap.Ber"), "name");
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
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("dumpBER"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("ASN_APPLICATION;ASN_BIT_STRING;ASN_BOOLEAN;ASN_CONSTRUCTOR;ASN_CONTEXT;ASN_ENUMERATED;ASN_INTEGER;ASN_NULL;ASN_OBJECT_ID;ASN_OCTET_STR;ASN_PRIMITIVE;ASN_PRIVATE;ASN_SEQUENCE;ASN_SET;ASN_SIMPLE_STRING;ASN_UNIVERSAL;buf;bufsize;offset"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
