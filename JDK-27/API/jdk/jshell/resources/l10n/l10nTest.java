/*
 * @test
 * @summary jdk.jshell.resources.l10n conformance: internal class reflection.
 * @modules jdk.jshell/jdk.jshell.resources:+open
 * @run main/othervm/timeout=180 --add-opens=jdk.jshell/jdk.jshell.resources=ALL-UNNAMED l10nTest
 */
public class l10nTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: the JShell core message catalog ----
    static final String[] EXPECTED_KEYS = {
        "jshell.diag.modifier.plural.fatal", "jshell.diag.modifier.plural.ignore",
        "jshell.diag.modifier.single.fatal", "jshell.diag.modifier.single.ignore",
        "jshell.diag.object.method.fatal", "jshell.exc.alien", "jshell.exc.closed",
        "jshell.exc.null", "jshell.exc.var.not.valid" };

    static java.util.ResourceBundle bundle() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.resources.l10n");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        return (java.util.ResourceBundle) ctor.newInstance();
    }

    // 引擎消息目录会按默认 Locale 本地化（中文 Windows 上的 diagnostics/
    // 异常文本是中文）。按 ResourceBundle 的候选 Locale 顺序反射实例化对应
    // 资源包类，再把“模板中 {0} 之前/之后的部分”作为断言依据，避免把断言
    // 绑死在英文文案上；基础资源包（英文）仍由 bundle() 直接校验。
    static java.util.ResourceBundle localizedBundle() throws Exception {
        String base = "jdk.jshell.resources.l10n";
        java.util.ResourceBundle.Control ctl =
                java.util.ResourceBundle.Control.getControl(
                        java.util.ResourceBundle.Control.FORMAT_DEFAULT);
        for (java.util.Locale l : ctl.getCandidateLocales(base, java.util.Locale.getDefault())) {
            String className = ctl.toBundleName(base, l);
            try {
                Class<?> c = Class.forName(className);
                java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
                ctor.setAccessible(true);
                return (java.util.ResourceBundle) ctor.newInstance();
            } catch (ClassNotFoundException ignore) {
                // 该 Locale 没有翻译资源，继续按候选顺序回退
            }
        }
        throw new AssertionError("no engine catalog bundle for " + java.util.Locale.getDefault());
    }

    static String template(String key) throws Exception {
        return localizedBundle().getString(key);
    }

    static String beforePlaceholder(String pattern) {
        int i = pattern.indexOf("{0}");
        return i < 0 ? pattern : pattern.substring(0, i);
    }

    static String afterPlaceholder(String pattern) {
        int i = pattern.indexOf("{0}");
        return i < 0 ? "" : pattern.substring(i + 3);
    }

    static void testMessageCatalog() throws Exception {
        java.util.ResourceBundle rb = bundle();
        java.util.Set<String> keys = new java.util.TreeSet<>(rb.keySet());
        java.util.Set<String> want = new java.util.TreeSet<>(java.util.Arrays.asList(EXPECTED_KEYS));
        check(keys.equals(want), "exact key set of the engine catalog: " + keys);
        check(rb.getString("jshell.exc.null").equals("Snippet must not be null"), "jshell.exc.null text");
        check(rb.getString("jshell.exc.closed").equals("JShell ({0}) has been closed."), "jshell.exc.closed has {0} placeholder");
        check(rb.getString("jshell.exc.alien").startsWith("Snippet not from this JShell: {0}"), "jshell.exc.alien has {0} placeholder");
        check(rb.getString("jshell.exc.var.not.valid").contains("{0}") && rb.getString("jshell.exc.var.not.valid").contains("{1}"),
              "jshell.exc.var.not.valid has two placeholders");
        check(rb.getString("jshell.diag.object.method.fatal").startsWith("JShell method names must not match Object methods: {0}"),
              "object-method diagnostic text");
        // all messages are MessageFormat-renderable with their placeholders
        for (String k : EXPECTED_KEYS) {
            String rendered = new java.text.MessageFormat(rb.getString(k)).format(new Object[]{"x", "y"});
            check(!rendered.isEmpty(), "MessageFormat renders " + k);
            if (rb.getString(k).contains("{0}"))
                check(rendered.contains("x"), "placeholder {0} filled for " + k);
        }
        // the {0}-filled messages are exactly what the engine reports
        String omPrefix = beforePlaceholder(template("jshell.diag.object.method.fatal"));
        try (jdk.jshell.JShell js = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            jdk.jshell.Snippet bad = js.eval("boolean equals(Object o) { return false; }").get(0).snippet();
            boolean saw = false;
            for (jdk.jshell.Diag d : js.diagnostics(bad).toList())
                if (d.getMessage(null).startsWith(omPrefix)) saw = true;
            check(saw, "engine surfaces jshell.diag.object.method.fatal through diagnostics");
        }
        String closedPattern = template("jshell.exc.closed");
        String closedPrefix = beforePlaceholder(closedPattern);
        String closedSuffix = afterPlaceholder(closedPattern);
        jdk.jshell.JShell closed = jdk.jshell.JShell.builder().executionEngine("local").build();
        closed.close();
        try { closed.eval("1"); } catch (IllegalStateException expected) {
            check(expected.getMessage().startsWith(closedPrefix)
                      && expected.getMessage().endsWith(closedSuffix),
                  "engine surfaces jshell.exc.closed on a closed shell");
        }
        String alienPrefix = beforePlaceholder(template("jshell.exc.alien"));
        try (jdk.jshell.JShell a = jdk.jshell.JShell.builder().executionEngine("local").build();
             jdk.jshell.JShell b = jdk.jshell.JShell.builder().executionEngine("local").build()) {
            jdk.jshell.VarSnippet alien = (jdk.jshell.VarSnippet) a.eval("int av1 = 1;").get(0).snippet();
            try { b.varValue(alien); } catch (IllegalArgumentException expected) {
                check(expected.getMessage().startsWith(alienPrefix),
                      "engine surfaces jshell.exc.alien for a foreign snippet");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.resources.l10n", false, l10nTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.jshell.resources.l10n"), "name");
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
        testMessageCatalog();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
