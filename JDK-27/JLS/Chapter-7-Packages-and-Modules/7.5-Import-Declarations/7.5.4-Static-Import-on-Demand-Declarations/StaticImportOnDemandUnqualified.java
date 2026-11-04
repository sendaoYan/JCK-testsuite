/*
 * @test
 * @summary JLS 7.5.4: the TypeName of a static-import-on-demand
 *          declaration must be qualified; an unqualified type name is a
 *          compile-time error.
 * @compile/fail StaticImportOnDemandUnqualified.java
 */

import static Math.*;

class T {
    double d = sqrt(4.0);
}
