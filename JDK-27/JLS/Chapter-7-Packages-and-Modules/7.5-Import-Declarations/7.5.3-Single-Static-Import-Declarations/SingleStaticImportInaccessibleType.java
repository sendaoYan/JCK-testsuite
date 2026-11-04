/*
 * @test
 * @summary JLS 7.5.3: it is a compile-time error if the named type
 *          of a single-static-import declaration is not accessible.
 * @compile/fail q/Secret.java SingleStaticImportInaccessibleType.java
 */

package p;

import static q.Secret.x;

class T {
    int a = x;   // error: q.Secret is not accessible
}
