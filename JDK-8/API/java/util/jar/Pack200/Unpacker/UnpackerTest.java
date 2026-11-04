/*
 * @test
 * @summary java.util.jar.Pack200.Unpacker conformance: constants and properties (JDK-8 only).
 * @run main UnpackerTest
 */

import java.util.SortedMap;
import java.util.jar.Pack200;

public class UnpackerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnpacker() {
        check(Pack200.Unpacker.class.isInterface(), "is interface");
        check("keep".equals(Pack200.Unpacker.KEEP), "KEEP");
        check("true".equals(Pack200.Unpacker.TRUE), "TRUE");
        check("false".equals(Pack200.Unpacker.FALSE), "FALSE");
        check("unpack.deflate.hint".equals(Pack200.Unpacker.DEFLATE_HINT), "DEFLATE_HINT");
        check("unpack.progress".equals(Pack200.Unpacker.PROGRESS), "PROGRESS");

        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        SortedMap<String, String> props = unpacker.properties();
        check(props != null, "properties non-null");
        props.put(Pack200.Unpacker.DEFLATE_HINT, Pack200.Unpacker.TRUE);
        check(Pack200.Unpacker.TRUE.equals(props.get(Pack200.Unpacker.DEFLATE_HINT)),
              "set/get DEFLATE_HINT property");
    }

    public static void main(String[] args) {
        testUnpacker();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
