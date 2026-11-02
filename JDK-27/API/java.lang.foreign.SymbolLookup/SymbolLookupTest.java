/*
 * @test
 * @summary java.lang.foreign.SymbolLookup conformance: loaderLookup/or/find.
 * @run main SymbolLookupTest
 */
import java.lang.foreign.SymbolLookup;

public class SymbolLookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(SymbolLookup.class.isInterface(), "is interface");
        SymbolLookup loader = SymbolLookup.loaderLookup();
        check(loader != null, "loaderLookup non-null");
        check(loader.find("nonexistent_symbol_xyz").isEmpty(), "find nonexistent empty");
        check(loader.or(SymbolLookup.loaderLookup()) != null, "or non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
