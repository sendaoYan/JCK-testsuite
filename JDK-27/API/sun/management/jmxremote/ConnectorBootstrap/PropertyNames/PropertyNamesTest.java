/*
 * @test
 * @summary sun.management.jmxremote.ConnectorBootstrap.PropertyNames conformance: internal interface reflection.
 * @modules jdk.management.agent/sun.management.jmxremote:+open
 * @run main PropertyNamesTest
 */
public class PropertyNamesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jmxremote.ConnectorBootstrap$PropertyNames", false, PropertyNamesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.management.jmxremote.ConnectorBootstrap$PropertyNames"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
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
            check(String.join(";", s).equals(""), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("ACCESS_FILE_NAME;CONFIG_FILE_NAME;HASH_PASSWORDS;HOST;LOCAL_PORT;LOGIN_CONFIG_NAME;PASSWORD_FILE_NAME;PORT;RMI_PORT;SERIAL_FILTER_PATTERN;SSL_CONFIG_FILE_NAME;SSL_ENABLED_CIPHER_SUITES;SSL_ENABLED_PROTOCOLS;SSL_NEED_CLIENT_AUTH;USE_AUTHENTICATION;USE_LOCAL_ONLY;USE_REGISTRY_SSL;USE_SSL"), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
