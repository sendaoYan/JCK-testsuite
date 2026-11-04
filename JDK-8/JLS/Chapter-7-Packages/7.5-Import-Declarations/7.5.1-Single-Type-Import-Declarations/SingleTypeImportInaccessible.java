/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): it is a compile-time error if the named type
 *          of a single-type-import declaration is not accessible (importing a
 *          package-private class from another package).
 * @compile/fail q/Secret.java SingleTypeImportInaccessible.java
 */

package p;

import q.Secret;

class T {}
