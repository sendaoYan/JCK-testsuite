/*
 * @test
 * @summary JLS 14.11: case labels for a switch over an enum may use the enum
 *          constant's unqualified name; fall-through and default still apply.
 * @run main SwitchEnumUnqualified
 */

enum Season { SPRING, SUMMER, AUTUMN, WINTER }

public class SwitchEnumUnqualified {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static int ordinalByName(Season s) {
        int r = -1;
        switch (s) {
            case SPRING: r = 0; break;
            case SUMMER: r = 1; break;
            case AUTUMN: r = 2; break;
            case WINTER: r = 3; break;
            default: r = 99;
        }
        return r;
    }

    public static void main(String[] args) {
        check(ordinalByName(Season.SPRING) == 0, "SPRING");
        check(ordinalByName(Season.SUMMER) == 1, "SUMMER");
        check(ordinalByName(Season.AUTUMN) == 2, "AUTUMN");
        check(ordinalByName(Season.WINTER) == 3, "WINTER");

        // fall-through over enum cases.
        int n = 0;
        switch (Season.SPRING) {
            case SPRING: n += 1;   // falls through
            case SUMMER: n += 2;   // executed by fall-through
                break;
            default: n += 100;
        }
        check(n == 3, "enum fall-through");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
