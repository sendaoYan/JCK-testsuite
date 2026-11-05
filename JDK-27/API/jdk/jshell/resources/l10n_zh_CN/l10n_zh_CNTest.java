/*
 * @test
 * @summary jdk.jshell.resources.l10n_zh_CN conformance: internal class reflection.
 * @modules jdk.jshell/jdk.jshell.resources:+open
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell.resources=ALL-UNNAMED l10n_zh_CNTest
 */
public class l10n_zh_CNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: complete translation of the engine catalog ----
    static void testLocalizedCatalog() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.resources.l10n_zh_CN");
        check(c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "extends ListResourceBundle");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        java.util.ResourceBundle rb = (java.util.ResourceBundle) ctor.newInstance();
        java.util.ResourceBundle base = baseBundle();
        check(rb.keySet().equals(base.keySet()), "same key set as the base bundle");
        // every value is translated (differs from English) and non-empty
        int translated = 0;
        for (String k : rb.keySet()) {
            check(!rb.getString(k).isEmpty(), "translated value non-empty: " + k);
            if (!rb.getString(k).equals(base.getString(k))) translated++;
        }
        check(translated == rb.keySet().size(), "all " + rb.keySet().size() + " messages localized (got " + translated + ")");

        check(rb.getString("jshell.exc.null").equals("片段不能为空值"), "Simplified-Chinese null-message");

        // placeholder counts match the English template
        for (String k : rb.keySet())
            check(rb.getString(k).replaceAll("[^{}]", "").replaceAll("\\{\\}", "").length()
                  == base.getString(k).replaceAll("[^{}]", "").replaceAll("\\{\\}", "").length(),
                  "placeholder structure preserved: " + k);
    }

    static java.util.ResourceBundle baseBundle() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.resources.l10n");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        return (java.util.ResourceBundle) ctor.newInstance();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.resources.l10n_zh_CN", false, l10n_zh_CNTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.jshell.resources.l10n_zh_CN"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
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
            check(String.join(";", s).equals("getContents"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        testLocalizedCatalog();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
