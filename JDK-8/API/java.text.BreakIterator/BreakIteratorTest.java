/*
 * @test
 * @summary java.text.BreakIterator conformance: word/character iteration, first/last/next/
 *          DONE, isBoundary.
 * @run main BreakIteratorTest
 */

import java.text.BreakIterator;
import java.util.Locale;

public class BreakIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBreakIterator() {
        BreakIterator bi = BreakIterator.getWordInstance(Locale.US);
        bi.setText("Hello world");
        check(bi.first() == 0, "first");
        check(bi.current() == 0, "current after first");
        check(bi.last() == 11, "last");
        bi.first();
        int boundaries = 0;
        while (bi.next() != BreakIterator.DONE) {
            boundaries++;
        }
        check(boundaries > 2, "word boundaries found");

        bi.setText("abc def");
        check(bi.isBoundary(3), "isBoundary at space");
        check(!bi.isBoundary(1), "isBoundary false inside word");
        StringBuilder sb = new StringBuilder();
        java.text.CharacterIterator ci = bi.getText();
        for (char c = ci.first(); c != java.text.CharacterIterator.DONE; c = ci.next()) {
            sb.append(c);
        }
        check("abc def".equals(sb.toString()), "getText reconstructs source");

        BreakIterator wb = BreakIterator.getWordInstance(Locale.US);
        wb.setText(new java.text.StringCharacterIterator("abc def"));
        check(wb.isBoundary(3), "isBoundary after setText(CharacterIterator)");

        check(BreakIterator.getCharacterInstance() != null, "getCharacterInstance");
        check(BreakIterator.getLineInstance() != null, "getLineInstance");
        check(BreakIterator.getSentenceInstance() != null, "getSentenceInstance");
        check(BreakIterator.getAvailableLocales().length > 0, "getAvailableLocales");
        check(BreakIterator.DONE == -1, "DONE == -1");
    }

    public static void main(String[] args) {
        testBreakIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
