/*
 * @test
 * @summary JLS 7.5.3 (Java SE 8): a single-static-import that imports a type
 *          whose simple name is n conflicts with a top level type named n in the
 *          same compilation unit; a compile-time error occurs.
 * @compile/fail q/Holder.java SingleStaticImportConflictsWithTopLevelType.java
 */

package p;

import static q.Holder.Entry;

class Entry {}
