/*
 * @test
 * @summary JLS 8.9.3: each enum constant yields an implicit public static final
 *          field, plus implicitly declared values() and valueOf(String) methods.
 * @run main EnumImplicitMembers
 */

public class EnumImplicitMembers {
    enum Season { WINTER, SPRING, SUMMER, FALL }

    public static void main(String[] args) {
        if (Season.WINTER == null || Season.SPRING == null
            || Season.SUMMER == null || Season.FALL == null)
            throw new AssertionError("implicit fields");

        Season[] all = Season.values();
        if (all.length != 4) throw new AssertionError("values length");

        if (Season.valueOf("FALL") != Season.FALL) throw new AssertionError("valueOf");

        try {
            Season.valueOf("NOPE");
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // valueOf throws IllegalArgumentException for an unknown name
        }
        System.out.println("PASS");
    }
}
