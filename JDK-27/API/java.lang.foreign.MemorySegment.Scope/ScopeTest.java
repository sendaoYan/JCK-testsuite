/*
 * @test
 * @summary java.lang.foreign.MemorySegment.Scope conformance: interface and isAlive/equals/hashCode.
 * @run main ScopeTest
 */
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class ScopeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(MemorySegment.Scope.class.isInterface(), "is interface");
        try (Arena a = Arena.ofConfined()) {
            MemorySegment.Scope scope = a.scope();
            check(scope.isAlive(), "isAlive");
            check(scope.equals(scope), "equals self");
            check(scope.hashCode() == scope.hashCode(), "hashCode stable");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
