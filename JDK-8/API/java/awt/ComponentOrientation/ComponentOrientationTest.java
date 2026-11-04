/*
 * @test
 * @summary java.awt.ComponentOrientation conformance: left-to-right/right-to-left.
 * @run main ComponentOrientationTest
 */


import java.awt.ComponentOrientation;
import java.util.Locale;
public class ComponentOrientationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ComponentOrientation ltr = ComponentOrientation.getOrientation(Locale.US);
        check(ltr.isHorizontal() && ltr.isLeftToRight(), "US is LTR horizontal");
        ComponentOrientation rtl = ComponentOrientation.getOrientation(new Locale("ar"));
        check(rtl.isHorizontal() && !rtl.isLeftToRight(), "Arabic is RTL horizontal");
        check(ComponentOrientation.LEFT_TO_RIGHT.isLeftToRight(), "LEFT_TO_RIGHT constant");
        check(!ComponentOrientation.RIGHT_TO_LEFT.isLeftToRight(), "RIGHT_TO_LEFT constant");
        check(ComponentOrientation.UNKNOWN.isHorizontal(), "UNKNOWN is horizontal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
