/*
 * @test
 * @summary JLS 8.10.1: it is a compile-time error for a record declaration to have
 *          two record components with the same name.
 * @compile/fail DuplicateRecordComponentName.java
 */

record DuplicateRecordComponentName(int x, int x) {}   // error: duplicate component
