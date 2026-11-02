/*
 * @test
 * @summary java.lang.foreign.Linker conformance: interface and nativeLinker/defaultLookup/canonicalLayouts.
 * @run main LinkerTest
 */
import java.lang.foreign.Linker;

public class LinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Linker.class.isInterface(), "is interface");
        Linker linker = Linker.nativeLinker();
        check(linker != null, "nativeLinker non-null");
        check(linker.defaultLookup() != null, "defaultLookup non-null");
        check(!linker.canonicalLayouts().isEmpty(), "canonicalLayouts non-empty");
        check(linker.canonicalLayouts().containsKey("int"), "canonicalLayouts has int");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
