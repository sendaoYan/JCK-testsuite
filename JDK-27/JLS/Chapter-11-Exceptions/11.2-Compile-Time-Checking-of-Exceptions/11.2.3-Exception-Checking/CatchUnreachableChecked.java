/*
 * @test
 * @summary JLS 11.2.3: a catch clause may not catch a checked exception class that
 *          the corresponding try block cannot throw.
 * @compile/fail CatchUnreachableChecked.java
 */

class CatchUnreachableChecked {
    void m() {
        try {
            int x = 1;
        } catch (java.io.IOException e) {   // error: never thrown in the try block
        }
    }
}
