/*
 * @test
 * @summary JLS 7.5.1 Example 7.5.1-2: a single-type-import of a class whose
 *          simple name equals a top level class declared in the same compilation
 *          unit is a compile-time error.
 * @compile/fail SingleTypeImportConflictsWithLocal.java
 */

import java.util.Vector;

class Vector { Object[] vec; }   // error: conflicts with imported java.util.Vector
