/*
 * @test
 * @summary JLS 11.1.1: Throwable, Exception, RuntimeException, and Error have the
 *          specified class hierarchy relationships.
 * @run main ExceptionHierarchy
 */

public class ExceptionHierarchy {
    public static void main(String[] args) {
        if (Throwable.class.getSuperclass() != Object.class)
            throw new AssertionError("Throwable superclass");
        if (Exception.class.getSuperclass() != Throwable.class)
            throw new AssertionError("Exception superclass");
        if (Error.class.getSuperclass() != Throwable.class)
            throw new AssertionError("Error superclass");
        if (RuntimeException.class.getSuperclass() != Exception.class)
            throw new AssertionError("RuntimeException superclass");
        System.out.println("PASS");
    }
}
