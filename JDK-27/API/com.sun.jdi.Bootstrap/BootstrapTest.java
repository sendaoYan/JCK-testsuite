/*
 * @test
 * @summary com.sun.jdi.Bootstrap conformance: virtualMachineManager factory.
 * @run main BootstrapTest
 */

public class BootstrapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        com.sun.jdi.VirtualMachineManager vmm = com.sun.jdi.Bootstrap.virtualMachineManager();
        check(vmm != null, "virtualMachineManager non-null");
        check(vmm instanceof com.sun.jdi.VirtualMachineManager, "instanceof VirtualMachineManager");
        check(vmm.majorInterfaceVersion() >= 1, "majorInterfaceVersion >= 1");
        check(vmm.minorInterfaceVersion() >= 0, "minorInterfaceVersion >= 0");
        check(!vmm.allConnectors().isEmpty(), "allConnectors non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
