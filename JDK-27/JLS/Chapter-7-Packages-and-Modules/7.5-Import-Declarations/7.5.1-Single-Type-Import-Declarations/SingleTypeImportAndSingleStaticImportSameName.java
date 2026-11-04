/*
 * @test
 * @summary JLS 7.5.1: a compilation unit containing both a
 *          single-type-import of a type whose simple name is n and a
 *          single-static-import of a type whose simple name is n is a
 *          compile-time error.
 * @compile/fail q/Holder.java SingleTypeImportAndSingleStaticImportSameName.java
 */

package p;

import java.util.Map.Entry;
import static q.Holder.Entry;

class T {
    Entry e;   // error: two imports both declare Entry
}
