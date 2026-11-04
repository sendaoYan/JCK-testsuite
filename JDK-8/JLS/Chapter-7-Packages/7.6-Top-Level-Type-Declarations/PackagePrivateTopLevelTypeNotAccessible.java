/*
 * @test
 * @summary JLS 7.6 / 6.6.1 (Java SE 8): a package-access top level type is not
 *          accessible from a different package; importing it is a compile-time
 *          error.
 * @compile/fail p/PackagePrivate.java PackagePrivateTopLevelTypeNotAccessible.java
 */

package other;

import p.PackagePrivate;

class T {
    PackagePrivate pp;   // error: p.PackagePrivate is not public
}
