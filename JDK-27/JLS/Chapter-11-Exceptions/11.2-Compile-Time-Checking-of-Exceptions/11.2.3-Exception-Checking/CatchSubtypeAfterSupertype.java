/*
 * @test
 * @summary JLS 11.2.3: a catch clause may not catch an exception class that a
 *          preceding catch clause of the same try statement already catches.
 * @compile/fail CatchSubtypeAfterSupertype.java
 */

import java.io.FileNotFoundException;
import java.io.IOException;

class CatchSubtypeAfterSupertype {
    void m() {
        try {
            throw new FileNotFoundException();
        } catch (IOException e) {
        } catch (FileNotFoundException e) {   // error: already caught
        }
    }
}
