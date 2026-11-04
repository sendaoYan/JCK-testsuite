/*
 * @test
 * @summary java.util.jar.Pack200.Packer conformance: constants and properties (JDK-8 only).
 * @run main PackerTest
 */

import java.util.SortedMap;
import java.util.jar.Pack200;

public class PackerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPacker() {
        check(Pack200.Packer.class.isInterface(), "is interface");
        check("pack.segment.limit".equals(Pack200.Packer.SEGMENT_LIMIT), "SEGMENT_LIMIT");
        check("pack.effort".equals(Pack200.Packer.EFFORT), "EFFORT");
        check("pack.deflate.hint".equals(Pack200.Packer.DEFLATE_HINT), "DEFLATE_HINT");
        check("pack.modification.time".equals(Pack200.Packer.MODIFICATION_TIME),
              "MODIFICATION_TIME");
        check("keep".equals(Pack200.Packer.KEEP), "KEEP");
        check("pass".equals(Pack200.Packer.PASS), "PASS");
        check("strip".equals(Pack200.Packer.STRIP), "STRIP");
        check("error".equals(Pack200.Packer.ERROR), "ERROR");
        check("true".equals(Pack200.Packer.TRUE), "TRUE");
        check("false".equals(Pack200.Packer.FALSE), "FALSE");
        check("latest".equals(Pack200.Packer.LATEST), "LATEST");

        Pack200.Packer packer = Pack200.newPacker();
        SortedMap<String, String> props = packer.properties();
        check(props != null, "properties non-null");
        check(props.containsKey(Pack200.Packer.SEGMENT_LIMIT), "properties has SEGMENT_LIMIT");
    }

    public static void main(String[] args) {
        testPacker();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
