/*
 * @test
 * @summary java.util.Locale.IsoCountryCode conformance: enum PART1_ALPHA2/PART1_ALPHA3/PART3.
 * @run main IsoCountryCodeTest
 */
import java.util.Locale;

public class IsoCountryCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Locale.IsoCountryCode.class.isEnum(), "is enum");
        check(Locale.IsoCountryCode.PART1_ALPHA2 != null, "PART1_ALPHA2");
        check(Locale.IsoCountryCode.PART1_ALPHA3 != null, "PART1_ALPHA3");
        check(Locale.IsoCountryCode.PART3 != null, "PART3");
        check(Locale.IsoCountryCode.values().length == 3, "three constants");
        check(Locale.IsoCountryCode.valueOf("PART3") == Locale.IsoCountryCode.PART3, "valueOf");
        check(Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).contains("US"), "getISOCountries contains US");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
