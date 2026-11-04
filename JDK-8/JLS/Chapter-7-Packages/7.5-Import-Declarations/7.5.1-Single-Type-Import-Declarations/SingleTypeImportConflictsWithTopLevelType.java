/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): a single-type-import of a type whose simple
 *          name equals a top level type declared in the same compilation unit is
 *          a compile-time error.
 * @compile/fail SingleTypeImportConflictsWithTopLevelType.java
 */

import java.util.Vector;

class Vector { Object[] vec; }
