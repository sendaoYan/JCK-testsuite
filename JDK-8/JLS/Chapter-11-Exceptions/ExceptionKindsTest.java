/*
 * @test
 * @summary JLS 11.1 (Java SE 8): exceptions are Throwable objects; checked
 *          exceptions are subclasses of Exception but not RuntimeException, while
 *          Error and RuntimeException are unchecked.
 * @run main ExceptionKindsTest
 */

public class ExceptionKindsTest {
    static class Checked extends Exception {}
    static class Unchecked extends RuntimeException {}
    static class MyError extends Error {}

    public static void main(String[] args) {
        Throwable t = new Checked();
        if (!(t instanceof Exception)) throw new AssertionError("checked is Exception");
        if (t instanceof RuntimeException) throw new AssertionError("checked not unchecked");

        Throwable u = new Unchecked();
        if (!(u instanceof RuntimeException)) throw new AssertionError("unchecked is RuntimeException");

        Throwable e = new MyError();
        if (!(e instanceof Error)) throw new AssertionError("Error kind");

        System.out.println("PASS");
    }
}
