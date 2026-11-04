/*
 * @test
 * @summary JLS 11.2.3 (Java SE 8): each alternative of a multi-catch clause must
 *          not already be caught by a preceding catch clause.
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
