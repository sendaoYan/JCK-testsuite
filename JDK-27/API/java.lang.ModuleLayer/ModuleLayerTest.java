/*
 * @test
 * @summary java.lang.ModuleLayer conformance: boot/empty/configuration/findModule.
 * @run main ModuleLayerTest
 */
import java.lang.ModuleLayer;

public class ModuleLayerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        ModuleLayer boot = ModuleLayer.boot();
        check(boot != null, "boot non-null");
        check(boot.findModule("java.base").isPresent(), "boot has java.base");
        check(boot.modules().size() > 0, "boot modules non-empty");
        check(boot.configuration().modules().size() > 0, "boot configuration non-empty");
        check(ModuleLayer.empty().modules().isEmpty(), "empty layer");
        check(boot.findLoader("java.base") == null, "findLoader java.base bootstrap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
