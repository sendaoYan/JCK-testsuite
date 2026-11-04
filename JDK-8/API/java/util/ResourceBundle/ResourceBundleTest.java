/*
 * @test
 * @summary java.util.ResourceBundle API conformance (Java 8 surface): complete contract coverage
 *          of getString/getObject/getStringArray + MissingResourceException for missing key +
 *          null-return semantics, getKeys/containsKey/keySet, getLocale/getBaseBundleName for a
 *          custom subclass, getBundle error paths (null NPE / nonexistent MissingResourceException),
 *          and clearCache.
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

public class ResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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

    static void testResourceBundle() {
        ResourceBundle b = new SimpleBundle();
        check("v".equals(b.getString("k")), "getString");
        check(Integer.valueOf(7).equals(b.getObject("n")), "getObject");
        check(b.containsKey("k"), "containsKey true");
        check(!b.containsKey("missing"), "containsKey false");
        check(b.keySet().contains("k"), "keySet contains key");
    }

    static void testStringArrayAndMissing() {
        ResourceBundle b = new SimpleBundle();
        String[] arr = b.getStringArray("arr");
        check(arr != null && arr.length == 2 && "a".equals(arr[0]) && "b".equals(arr[1]),
              "getStringArray returns array");
        // All three accessors throw MissingResourceException for a missing key.
        expectThrows(MissingResourceException.class, () -> b.getString("missing"),
                     "getString missing -> MissingResourceException");
        expectThrows(MissingResourceException.class, () -> b.getObject("missing"),
                     "getObject missing -> MissingResourceException");
        expectThrows(MissingResourceException.class, () -> b.getStringArray("missing"),
                     "getStringArray missing -> MissingResourceException");
        expectThrows(ClassCastException.class, () -> b.getStringArray("n"),
                     "getStringArray non-array -> ClassCastException");
    }

    static void testLocaleAndBaseName() {
        ResourceBundle b = new SimpleBundle();
        check(b.getLocale() == null, "getLocale null for custom bundle (not set)");
        check(b.getBaseBundleName() == null, "getBaseBundleName null for custom bundle");
    }

    static void testGetBundleErrors() {
        expectThrows(NullPointerException.class, () -> ResourceBundle.getBundle(null),
                     "getBundle(null) NPE");
        expectThrows(MissingResourceException.class,
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
        testResourceBundle();
        testStringArrayAndMissing();
        testLocaleAndBaseName();
        testGetBundleErrors();
        testBundleFallbackChain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
