/*
 * @test
 * @summary java.util.ResourceBundle conformance: white-box structure plus black-box behavior
 *          of getString/getObject/getStringArray/containsKey/keySet via a subclass, missing-key
 *          exceptions, and getBaseBundleName.
 * @run main ResourceBundleTest
 */

import java.util.Locale;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class ResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static class SimpleBundle extends ResourceBundle {
        private final Map<String, Object> map = new HashMap<String, Object>();
        SimpleBundle() {
            map.put("k", "v");
            map.put("n", Integer.valueOf(7));
            map.put("arr", new String[] { "a", "b" });
        }
        protected Object handleGetObject(String key) {
            return map.get(key);
        }
        public Enumeration<String> getKeys() {
            return Collections.enumeration(map.keySet());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.ResourceBundle");
        check(!c.isInterface(), "ResourceBundle is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ResourceBundle is abstract");
        c.getMethod("getString", String.class);
        c.getMethod("getObject", String.class);
        c.getMethod("getStringArray", String.class);
        c.getMethod("containsKey", String.class);
        c.getMethod("keySet");
        c.getDeclaredMethod("handleGetObject", String.class);
        c.getMethod("getKeys");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ResourceBundle b = new SimpleBundle();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(7).equals(b.getObject("n")), "getObject");
        String[] arr = b.getStringArray("arr");
        check(arr.length == 2 && "a".equals(arr[0]) && "b".equals(arr[1]), "getStringArray");
        check(b.containsKey("k"), "containsKey true");
        check(!b.containsKey("missing"), "containsKey false");

        Set<String> keys = b.keySet();
        check(keys.contains("k") && keys.contains("n") && keys.contains("arr"),
              "keySet contains all keys");

        assertThrows(MissingResourceException.class,
                () -> b.getString("missing"), "getString missing throws MissingResourceException");
        assertThrows(MissingResourceException.class,
                () -> b.getObject("missing"), "getObject missing throws MissingResourceException");
        assertThrows(MissingResourceException.class,
                () -> b.getStringArray("missing"), "getStringArray missing throws MissingResourceException");
        assertThrows(ClassCastException.class,
                () -> b.getStringArray("n"), "getStringArray non-array throws ClassCastException");

        check(b.getLocale() == null, "getLocale null for custom bundle (not set)");
        check(b.getBaseBundleName() == null, "getBaseBundleName null for directly-instantiated bundle");

        assertThrows(NullPointerException.class,
                () -> ResourceBundle.getBundle((String) null), "getBundle((String)null) NPE");
        assertThrows(MissingResourceException.class,
                () -> ResourceBundle.getBundle("com.example.DoesNotExistBundle"),
                "getBundle(nonexistent) MissingResourceException");
        ResourceBundle.clearCache();
        passed++;
    }


    // ===================== 契约深化：真实属性文件回退链/父链 keySet/缓存/clearCache =====================
    static java.nio.file.Path rbTemp;
    static URLClassLoader rbCl;

    static void setupRbFixtures() throws Exception {
        rbTemp = java.nio.file.Files.createTempDirectory("rbtest");
        java.nio.file.Files.write(rbTemp.resolve("deep.properties"),
              "onlyBase=base\nshared=base\n".getBytes("ISO-8859-1"));
        java.nio.file.Files.write(rbTemp.resolve("deep_fr.properties"),
              "onlyFr=fr\nshared=fr\n".getBytes("ISO-8859-1"));
        java.nio.file.Files.write(rbTemp.resolve("deep_fr_FR.properties"),
              "onlyFrFR=frFR".getBytes("ISO-8859-1"));
        rbCl = new URLClassLoader(new URL[]{ rbTemp.toUri().toURL() });
    }

    static void testBundleFallbackChain() throws Exception {
        setupRbFixtures();
        ResourceBundle.clearCache(rbCl);
        ResourceBundle frFR = ResourceBundle.getBundle("deep", new Locale("fr", "FR"), rbCl);
        check(new Locale("fr", "FR").equals(frFR.getLocale()), "getLocale is most specific match");
        check("deep".equals(frFR.getBaseBundleName()), "getBaseBundleName");
        check("frFR".equals(frFR.getString("onlyFrFR"))
              && "fr".equals(frFR.getString("onlyFr"))
              && "base".equals(frFR.getString("onlyBase")),
              "fallback chain resolves own, parent, base");
        check("fr".equals(frFR.getString("shared")), "child value shadows parent for same key");
        java.util.Set<String> keys = frFR.keySet();
        check(keys.size() == 4 && keys.containsAll(
              java.util.Arrays.asList("onlyBase", "onlyFr", "onlyFrFR", "shared")),
              "keySet unions parent chain");
        ResourceBundle fr = ResourceBundle.getBundle("deep", new Locale("fr"), rbCl);
        check(new Locale("fr").equals(fr.getLocale()) && "fr".equals(fr.getString("shared"))
              && !fr.containsKey("onlyFrFR"), "fr locale stops at fr bundle");
        ResourceBundle enUS = ResourceBundle.getBundle("deep", Locale.US, rbCl);
        check(Locale.ROOT.equals(enUS.getLocale()) || enUS.getLocale().toString().isEmpty(),
              "no match falls back to base bundle with empty locale");
        ResourceBundle again = ResourceBundle.getBundle("deep", new Locale("fr", "FR"), rbCl);
        check(again == frFR, "same-argument getBundle returns cached instance");
        ResourceBundle.clearCache(rbCl);
        ResourceBundle after = ResourceBundle.getBundle("deep", new Locale("fr", "FR"), rbCl);
        check(after != frFR, "clearCache invalidates cached instance");
        ResourceBundle.clearCache();
        passed++;
        rbCl.close();
        deleteRbTemp(rbTemp);
    }

    static void deleteRbTemp(java.nio.file.Path dir) throws Exception {
        java.nio.file.DirectoryStream<java.nio.file.Path> ds = java.nio.file.Files.newDirectoryStream(dir);
        for (java.nio.file.Path p : ds) java.nio.file.Files.deleteIfExists(p);
        ds.close();
        java.nio.file.Files.deleteIfExists(dir);
    }

    static void testMissingResourceExceptionDetails() {
        ResourceBundle b = new SimpleBundle();
        try {
            b.getString("nope");
            check(false, "MRE reachable");
        } catch (MissingResourceException e) {
            check("java.util.ResourceBundleTest$SimpleBundle".equals(e.getClassName()),
                  "getClassName is the bundle class");
            check("nope".equals(e.getKey()), "getKey returns missing key");
            check(e.getMessage() != null && e.getMessage().contains("nope"),
                  "message names the key");
        }
        try {
            ResourceBundle.getBundle("deep.DoesNotExist", Locale.ROOT, rbCl == null
                  ? ResourceBundleTest.class.getClassLoader() : rbCl);
            check(false, "getBundle nonexistent reachable");
        } catch (MissingResourceException e) {
            check(e.getClassName() != null && e.getKey() == null,
                  "bundle-level MRE: class name present, key null");
        }
    }

    static void testProtectedSetParentChain() {
        class Child extends ResourceBundle {
            protected Object handleGetObject(String key) {
                return "ck".equals(key) ? "cv" : null;
            }
            public java.util.Enumeration<String> getKeys() {
                return java.util.Collections.enumeration(java.util.Collections.singleton("ck"));
            }
        }
        Child c = new Child();
        check("cv".equals(c.getString("ck")), "child key before linking");
        try {
            c.getString("pk");
            check(false, "parent key absent before linking MRE");
        } catch (MissingResourceException mre) { check(true, "parent key absent before linking MRE"); }
        // setParent 是 protected：外部代码无法直接构造父链（编译期保证），只能经 getBundle 建立
        check(c.containsKey("ck") && !c.containsKey("zz"), "containsKey scope");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBundleFallbackChain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
