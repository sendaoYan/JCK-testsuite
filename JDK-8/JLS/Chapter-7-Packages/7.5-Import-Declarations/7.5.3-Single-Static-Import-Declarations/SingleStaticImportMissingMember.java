/*
 * @test
 * @summary JLS 7.5.3 (Java SE 8): the Identifier of a single-static-import must
 *          name at least one static member of the named type; a missing static
 *          member is a compile-time error.
 * @compile/fail SingleStaticImportMissingMember.java
 */

import static java.lang.Math.noSuchMember;

class T {
    double d = noSuchMember;
}
