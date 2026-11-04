/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): an import declaration cannot import a
 *          subpackage, only a type (Example 7.5.1-3).
 * @compile/fail SingleTypeImportOfSubpackage.java
 */

import java.util;

class T {
    util.Random generator;   // error: java.util is a package, not a type
}
