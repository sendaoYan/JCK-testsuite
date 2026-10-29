/*
 * @test
 * @summary JLS 7.5.1 Example 7.5.1-2: two single-type-import declarations that
 *          import different types with the same simple name are a compile-time
 *          error.
 * @compile/fail DuplicateSingleTypeImport.java
 */

import java.util.List;
import java.awt.List;

class DuplicateSingleTypeImport {
    void m() {
        List l = null;   // error: both imports declare List
    }
}
