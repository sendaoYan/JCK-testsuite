/*
 * @test
 * @summary JLS 7.5.4: it is a compile-time error if the named type
 *          of a static-import-on-demand declaration is not accessible.
 * @compile/fail q/Secret.java StaticImportOnDemandInaccessibleType.java
 */

package p;

import static q.Secret.*;

class T {
    int a = x;   // error: q.Secret is not accessible
}
