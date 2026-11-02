/*
 * @test
 * @summary java.security.spec.DSAGenParameterSpec conformance: constructors and accessors.
 * @run main DSAGenParameterSpecTest
 */

import java.security.spec.DSAGenParameterSpec;

public class DSAGenParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DSAGenParameterSpec spec = new DSAGenParameterSpec(2048, 256);
        check(spec.getPrimePLength() == 2048, "getPrimePLength");
        check(spec.getSubprimeQLength() == 256, "getSubprimeQLength");
        check(spec.getSeedLength() == 256, "getSeedLength default");
        DSAGenParameterSpec spec2 = new DSAGenParameterSpec(2048, 256, 512);
        check(spec2.getSeedLength() == 512, "getSeedLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
