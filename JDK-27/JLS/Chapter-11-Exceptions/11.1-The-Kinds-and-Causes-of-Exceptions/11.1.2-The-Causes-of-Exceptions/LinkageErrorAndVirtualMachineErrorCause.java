/*
 * @test
 * @summary JLS 11.1.2: an error while loading/linking/initializing throws a
 *          subclass of LinkageError; an internal error or resource limitation
 *          throws a subclass of VirtualMachineError. Both are subclasses of Error.
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

        check(LinkageError.class.isAssignableFrom(NoClassDefFoundError.class),
              "NoClassDefFoundError is a LinkageError");
        check(LinkageError.class.isAssignableFrom(VerifyError.class),
              "VerifyError is a LinkageError");
        check(LinkageError.class.isAssignableFrom(ClassFormatError.class),
              "ClassFormatError is a LinkageError");

        check(VirtualMachineError.class.isAssignableFrom(OutOfMemoryError.class),
              "OutOfMemoryError is a VirtualMachineError");
        check(VirtualMachineError.class.isAssignableFrom(StackOverflowError.class),
              "StackOverflowError is a VirtualMachineError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
