/*
 * @test
 * @summary JLS 7.7.5: an ordinary compilation unit that is not associated with a
 *          named module is associated with an unnamed module, which reads every
 *          observable module.
 * @run main UnnamedModule
 */

import java.lang.module.ModuleDescriptor;

public class UnnamedModule {
    public static void main(String[] args) {
        Module unnamed = UnnamedModule.class.getModule();

        // 7.7.5: not associated with a named module.
        if (unnamed.isNamed()) throw new AssertionError("expected unnamed module");
        if (unnamed.getName() != null) throw new AssertionError("name: " + unnamed.getName());
        if (unnamed.getDescriptor() != null) throw new AssertionError("unnamed module has a descriptor");

        // 7.7.5: an unnamed module reads every observable module.
        Module javaSql = ModuleLayer.boot().findModule("java.sql")
                .orElseThrow(() -> new AssertionError("java.sql not observable"));
        if (!unnamed.canRead(javaSql)) throw new AssertionError("unnamed module does not read java.sql");

        System.out.println("PASS");
    }
}
