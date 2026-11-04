/*
 * @test
 * @summary java.util.Locale.IsoCountryCode conformance: enum PART1_ALPHA2/PART1_ALPHA3/PART3.
 * @run main IsoCountryCodeTest
 */
import java.util.Locale;

public class IsoCountryCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }


    // ===================== 契约深化：三种 ISO 代码空间检索 =====================
    static void testIsoCountryCodeContracts() {
        java.util.Set<String> a2 = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);
        java.util.Set<String> a3 = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3);
        java.util.Set<String> p3 = Locale.getISOCountries(Locale.IsoCountryCode.PART3);
        check(a2.contains("US") && a2.contains("CN"), "PART1_ALPHA2 contains US/CN");
        for (String c : a2) checkLen(c, 2);
        check(a3.contains("USA") && a3.contains("CHN"), "PART1_ALPHA3 contains USA/CHN");
        for (String c : a3) checkLen(c, 3);
        check(p3.size() > 20, "PART3 non-trivial size (historically superseded codes)");
        for (String c : p3) checkLen(c, 4);
        check(Locale.IsoCountryCode.valueOf("PART1_ALPHA2") == Locale.IsoCountryCode.PART1_ALPHA2
              && Locale.IsoCountryCode.valueOf("PART1_ALPHA3") == Locale.IsoCountryCode.PART1_ALPHA3
              && Locale.IsoCountryCode.valueOf("PART3") == Locale.IsoCountryCode.PART3,
              "valueOf all three");
        check(Locale.IsoCountryCode.values().length == 3, "exactly three constants");
    }

    static void checkLen(String c, int n) {
        if (!(c.length() == n && c.equals(c.toUpperCase())))
            throw new AssertionError("ISO code shape (len " + n + "): " + c);
        passed++;
    }

    public static void main(String[] args) {
        check(Locale.IsoCountryCode.class.isEnum(), "is enum");
        check(Locale.IsoCountryCode.PART1_ALPHA2 != null, "PART1_ALPHA2");
        check(Locale.IsoCountryCode.PART1_ALPHA3 != null, "PART1_ALPHA3");
        check(Locale.IsoCountryCode.PART3 != null, "PART3");
        check(Locale.IsoCountryCode.values().length == 3, "three constants");
        check(Locale.IsoCountryCode.valueOf("PART3") == Locale.IsoCountryCode.PART3, "valueOf");
        check(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).contains("US"), "getISOCountries contains US");
        testIsoCountryCodeContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
