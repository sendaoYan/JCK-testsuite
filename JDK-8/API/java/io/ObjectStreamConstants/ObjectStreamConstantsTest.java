/*
 * @test
 * @summary java.io.ObjectStreamConstants API conformance: stream constants.
 * @run main ObjectStreamConstantsTest
 */

import java.io.ObjectStreamConstants;

public class ObjectStreamConstantsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectStreamConstants() {
        check(ObjectStreamConstants.STREAM_MAGIC == (short) 0xACED, "STREAM_MAGIC");
        check(ObjectStreamConstants.STREAM_VERSION == 5, "STREAM_VERSION");
        check(ObjectStreamConstants.TC_NULL == 0x70, "TC_NULL");
        check(ObjectStreamConstants.TC_OBJECT == 0x73, "TC_OBJECT");
        check(ObjectStreamConstants.TC_STRING == 0x74, "TC_STRING");
        check(ObjectStreamConstants.baseWireHandle == 0x7E0000, "baseWireHandle");
    }

    public static void main(String[] args) {
        testObjectStreamConstants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
