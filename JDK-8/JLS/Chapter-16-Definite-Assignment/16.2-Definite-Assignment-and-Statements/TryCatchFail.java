/*
 * @test
 * @summary JLS 16.2.15 (Java SE 8): a variable assigned in the try block but not in
 *          the catch block is not definitely assigned after the try statement.
 * @compile/fail TryCatchFail.java
 */

class TryCatchFail {
    void m() {
        int x;
        try { x = 1; } catch (RuntimeException e) { }
        int y = x;   // error: variable x might not have been initialized
    }
}
