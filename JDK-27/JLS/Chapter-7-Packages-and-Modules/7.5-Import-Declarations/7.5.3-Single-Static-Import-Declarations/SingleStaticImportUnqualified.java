/*
 * @test
 * @summary JLS 7.5.3: the TypeName of a single-static-import
 *          declaration must be qualified; an unqualified type name is a
 *          compile-time error.
 * @compile/fail SingleStaticImportUnqualified.java
 */

import static Math.max;

class T {
    int x = max(1, 2);
}
