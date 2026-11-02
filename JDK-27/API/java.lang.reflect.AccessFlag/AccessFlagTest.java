/*
 * @test
 * @summary java.lang.reflect.AccessFlag conformance: enum mask/sourceModifier/locations.
 * @run main AccessFlagTest
 */
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Modifier;

public class AccessFlagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(AccessFlag.class.isEnum(), "is enum");
        check(AccessFlag.PUBLIC.mask() == Modifier.PUBLIC, "PUBLIC mask");
        check(AccessFlag.STATIC.mask() == Modifier.STATIC, "STATIC mask");
        check(AccessFlag.STATIC.sourceModifier(), "STATIC sourceModifier");
        check(!AccessFlag.SYNTHETIC.sourceModifier(), "SYNTHETIC not source");
        check(AccessFlag.PUBLIC.locations().contains(AccessFlag.Location.CLASS), "PUBLIC locations CLASS");
        check(AccessFlag.maskToAccessFlags(Modifier.PUBLIC, AccessFlag.Location.CLASS).contains(AccessFlag.PUBLIC), "maskToAccessFlags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
