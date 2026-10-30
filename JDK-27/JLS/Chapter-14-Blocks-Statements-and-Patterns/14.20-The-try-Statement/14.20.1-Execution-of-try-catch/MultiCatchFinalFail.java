/*
 * @test
 * @summary JLS 14.20: an exception parameter of a multi-catch clause is
 *          implicitly declared final, so it cannot be assigned.
 * @compile/fail MultiCatchFinalFail.java
 */

import java.io.FileNotFoundException;

class MultiCatchFinalFail {
    void m() {
        try {
        } catch (FileNotFoundException | IllegalAccessException e) {
            e = new FileNotFoundException();   // error: implicitly final
        }
    }
}
