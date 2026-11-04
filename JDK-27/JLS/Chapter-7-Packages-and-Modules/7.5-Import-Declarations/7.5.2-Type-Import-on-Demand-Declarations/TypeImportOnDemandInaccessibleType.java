/*
 * @test
 * @summary JLS 7.5.2: a type-import-on-demand declaration naming an
 *          inaccessible type is a compile-time error.
 * @compile/fail q/Secret.java TypeImportOnDemandInaccessibleType.java
 */

package p;

import q.Secret.*;

class T {
    Inner i;   // error: q.Secret is not accessible
}
