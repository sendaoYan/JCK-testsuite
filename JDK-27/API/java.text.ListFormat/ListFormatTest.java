/*
 * @test
 * @summary java.text.ListFormat conformance: getInstance/format/parse/patterns.
 * @run main ListFormatTest
 */
import java.text.ListFormat;
import java.util.List;
import java.util.Locale;

public class ListFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        ListFormat shortF = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.SHORT);
        check("Apple & Banana".equals(shortF.format(List.of("Apple", "Banana"))), "SHORT two elements");
        ListFormat fullF = ListFormat.getInstance(Locale.US, ListFormat.Type.STANDARD, ListFormat.Style.FULL);
        check("Apple, Banana, and Cherry".equals(fullF.format(List.of("Apple", "Banana", "Cherry"))), "FULL three elements");
        check(shortF.getLocale().equals(Locale.US), "getLocale");
        check(shortF.getPatterns() != null && shortF.getPatterns().length > 0, "getPatterns");
        check(ListFormat.getAvailableLocales().length > 0, "getAvailableLocales");
        check(shortF.parse("Apple & Banana").size() == 2, "parse");
        check(shortF.format(List.of("solo")).equals("solo"), "single element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
