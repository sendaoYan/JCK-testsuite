/*
 * @test
 * @summary JLS 7.5.1 Example 7.5.1-3: an import declaration cannot import a
 *          subpackage, only a class or interface.
 * @compile/fail ImportSubpackage.java
 */

import java.util;   // error: java.util is a package, not a type

class ImportSubpackage {
    void m() {
    }
}
