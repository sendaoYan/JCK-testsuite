/*
 * @test
 * @summary JLS 11.1.3: asynchronous exceptions are instances of VirtualMachineError
 *          subclasses; StackOverflowError and OutOfMemoryError are such subclasses.
 * @run main AsynchronousExceptionKinds
 */

public class AsynchronousExceptionKinds {
    public static void main(String[] args) {
        if (!Error.class.isAssignableFrom(VirtualMachineError.class))
            throw new AssertionError("VirtualMachineError is an Error");
        if (!VirtualMachineError.class.isAssignableFrom(StackOverflowError.class))
            throw new AssertionError("StackOverflowError is a VirtualMachineError");
        if (!VirtualMachineError.class.isAssignableFrom(OutOfMemoryError.class))
            throw new AssertionError("OutOfMemoryError is a VirtualMachineError");
        System.out.println("PASS");
    }
}
