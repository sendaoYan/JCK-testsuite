/*
 * @test
 * @summary java.util.ResourceBundle.Control conformance: formats/candidates/bundle-name/ttl.
 * @run main ResourceBundleControlTest
 */

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleControlTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testControl() {
        ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        check(c != null, "getControl");
        List<String> formats = c.getFormats("x");
        check(formats.contains("java.class") && formats.contains("java.properties"), "getFormats");
        List<Locale> candidates = c.getCandidateLocales("base", Locale.US);
        check(!candidates.isEmpty() && candidates.get(0).equals(Locale.US),
              "getCandidateLocales starts with requested");
        check("base_en_US".equals(c.toBundleName("base", Locale.US)), "toBundleName");
        check(c.getTimeToLive("base", Locale.US) == ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL,
              "getTimeToLive default");
        check(ResourceBundle.Control.FORMAT_CLASS != ResourceBundle.Control.FORMAT_PROPERTIES,
              "format constants");
        check(ResourceBundle.Control.TTL_DONT_CACHE == -1, "TTL_DONT_CACHE");
        check(ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL == -2, "TTL_NO_EXPIRATION_CONTROL");
        ResourceBundle.Control noFallback =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
        check(noFallback != null, "getNoFallbackControl");
    }


    // ===================== 契约深化：候选列表全序/格式表/回退语义/非法参数矩阵 =====================
    static void testControlContracts() {
        ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        List<Locale> cand = c.getCandidateLocales("b", new Locale("fr", "FR"));
        check(cand.size() == 3 && cand.get(0).equals(new Locale("fr", "FR"))
              && cand.get(1).equals(new Locale("fr"))
              && cand.get(2).equals(Locale.ROOT),
              "candidate list fr_FR -> [fr_FR, fr, ROOT]");
        check(c.getCandidateLocales("b", Locale.ROOT).size() == 1
              && c.getCandidateLocales("b", Locale.ROOT).get(0).equals(Locale.ROOT),
              "ROOT locale candidate list is [ROOT]");
        java.util.List<String> formats = c.getFormats("b");
        check(formats.size() == 2 && "java.class".equals(formats.get(0))
              && "java.properties".equals(formats.get(1)),
              "FORMAT_DEFAULT order [java.class, java.properties]");
        check(java.util.Arrays.asList("java.class").equals(
              ResourceBundle.Control.getControl(java.util.Collections.singletonList("java.class"))
                    .getFormats("b")),
              "FORMAT_CLASS control");
        check("base".equals(c.toBundleName("base", Locale.ROOT)),
              "toBundleName ROOT -> bare base name");
        check("base_zh_CN".equals(c.toBundleName("base", new Locale("zh", "CN"))),
              "toBundleName appends language and country");
        check("my/pkg/B.properties".equals(c.toResourceName("my.pkg.B", "properties")),
              "toResourceName converts package and appends extension");
        Locale fb = c.getFallbackLocale("b", Locale.ROOT);
        Locale fmtDefault = Locale.getDefault(Locale.Category.FORMAT);
        Locale plainDefault = Locale.getDefault();
        check(fb == null || fb.equals(fmtDefault) || fb.equals(plainDefault),
              "getFallbackLocale returns null (when equal to default) or the default locale");
        try { c.getFallbackLocale("b", null); check(false, "getFallbackLocale(null) NPE"); }
        catch (NullPointerException npe) { check(true, "getFallbackLocale(null) NPE"); }
        try { c.getFormats(null); check(false, "getFormats(null) NPE"); }
        catch (NullPointerException npe) { check(true, "getFormats(null) NPE"); }
        try { c.getCandidateLocales("b", null); check(false, "getCandidateLocales null locale NPE"); }
        catch (NullPointerException npe) { check(true, "getCandidateLocales null locale NPE"); }
        try {
            ResourceBundle.Control.getControl(java.util.Collections.singletonList("bogus"));
            check(false, "getControl(unsupported formats) IAE");
        } catch (IllegalArgumentException iae) {
            check(true, "getControl(unsupported formats) IAE");
        }
        check(ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT) == c,
              "getControl returns cached singleton");
        check(ResourceBundle.Control.getNoFallbackControl(
              ResourceBundle.Control.FORMAT_DEFAULT) != null
              && ResourceBundle.Control.getNoFallbackControl(
                     ResourceBundle.Control.FORMAT_DEFAULT)
                 == ResourceBundle.Control.getNoFallbackControl(
                        ResourceBundle.Control.FORMAT_DEFAULT),
              "getNoFallbackControl cached singleton");
    }

    public static void main(String[] args) {
        testControl();
        testControlContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
