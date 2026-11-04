/*
 * @test
 * @summary java.io.FileDescriptor API conformance: the standard in/out/err descriptors are
 *          valid.
 * @run main FileDescriptorTest
 */

import java.io.FileDescriptor;

public class FileDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileDescriptor() {
        check(FileDescriptor.in.valid(), "standard in valid");
        check(FileDescriptor.out.valid(), "standard out valid");
        check(FileDescriptor.err.valid(), "standard err valid");
    }

    public static void main(String[] args) {
        testFileDescriptor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
