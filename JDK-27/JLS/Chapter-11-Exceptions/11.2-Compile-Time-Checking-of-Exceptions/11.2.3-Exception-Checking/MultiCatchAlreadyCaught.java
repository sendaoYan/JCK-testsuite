/*
 * @test
 * @summary JLS 11.2.3: each alternative of a multi-catch clause must be able to
 *          catch an exception class thrown by the try block and not already caught
 *          by a preceding catch clause.
 * @compile/fail MultiCatchAlreadyCaught.java
 */

class MultiCatchAlreadyCaught {
    static class Foo extends Exception {}
    static class Bar extends Exception {}

    void m() throws Foo, Bar {
        try {
            if (System.nanoTime() < 0) throw new Foo();
            throw new Bar();
        } catch (Foo f) {
        } catch (Bar | Foo e) {   // error: exception Foo has already been caught
        }
    }
}
