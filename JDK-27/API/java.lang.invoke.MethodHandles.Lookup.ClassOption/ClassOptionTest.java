/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup.ClassOption conformance: enum NESTMATE/STRONG.
 * @run main ClassOptionTest
 */
import java.lang.invoke.MethodHandles;

public class ClassOptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(MethodHandles.Lookup.ClassOption.class.isEnum(), "is enum");
        check(MethodHandles.Lookup.ClassOption.NESTMATE != null, "NESTMATE");
        check(MethodHandles.Lookup.ClassOption.STRONG != null, "STRONG");
        check(MethodHandles.Lookup.ClassOption.values().length == 2, "two constants");
        check(MethodHandles.Lookup.ClassOption.valueOf("STRONG") == MethodHandles.Lookup.ClassOption.STRONG, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
