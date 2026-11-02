/*
 * @test
 * @summary java.lang.reflect.AccessFlag.Location conformance: enum flagsMask/flags.
 * @run main LocationTest
 */
import java.lang.reflect.AccessFlag;

public class LocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(AccessFlag.Location.class.isEnum(), "is enum");
        check(AccessFlag.Location.CLASS.flags().contains(AccessFlag.PUBLIC), "CLASS.flags has PUBLIC");
        check(AccessFlag.Location.METHOD.flags().contains(AccessFlag.STATIC), "METHOD.flags has STATIC");
        check(AccessFlag.Location.FIELD.flags().contains(AccessFlag.VOLATILE), "FIELD.flags has VOLATILE");
        check(AccessFlag.Location.CLASS.flagsMask() != 0, "CLASS flagsMask non-zero");
        check(AccessFlag.Location.valueOf("MODULE") == AccessFlag.Location.MODULE, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
