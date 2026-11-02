/*
 * @test
 * @summary sun.util.locale.provider.FallbackLocaleProviderAdapter conformance: class and members.
 * @run main FallbackLocaleProviderAdapterTest
 */

public class FallbackLocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.FallbackLocaleProviderAdapter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.FallbackLocaleProviderAdapter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.JRELocaleProviderAdapter"), "superclass sun.util.locale.provider.JRELocaleProviderAdapter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createLanguageTagSet"), "declares method createLanguageTagSet");
        check(mnames.contains("getAdapterType"), "declares method getAdapterType");
        check(mnames.contains("getLocaleResources"), "declares method getLocaleResources");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
