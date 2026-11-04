/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): two single-type-import declarations importing
 *          different types with the same simple name are a compile-time error.
 * @compile/fail DuplicateSingleTypeImport.java
 */

import java.util.List;
import java.awt.List;

class T {
    void m() {
        List l = null;   // error: both imports declare List
    }
}
