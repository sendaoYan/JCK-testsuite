/*
 * @test
 * @summary JLS 11.1.2 (Java SE 8): loading/linking/initialization errors throw a
 *          subclass of LinkageError; internal errors or resource limitations throw
 *          a subclass of VirtualMachineError.
 * @run main LinkageErrorAndVirtualMachineErrorCause
 */

public class LinkageErrorAndVirtualMachineErrorCause {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Error.class.isAssignableFrom(LinkageError.class), "LinkageError is an Error");
        check(Error.class.isAssignableFrom(VirtualMachineError.class), "VirtualMachineError is an Error");
        check(LinkageError.class.isAssignableFrom(NoClassDefFoundError.class), "NoClassDefFoundError is a LinkageError");
        check(LinkageError.class.isAssignableFrom(ClassFormatError.class), "ClassFormatError is a LinkageError");
        check(VirtualMachineError.class.isAssignableFrom(OutOfMemoryError.class), "OutOfMemoryError is a VirtualMachineError");
        check(VirtualMachineError.class.isAssignableFrom(StackOverflowError.class), "StackOverflowError is a VirtualMachineError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
