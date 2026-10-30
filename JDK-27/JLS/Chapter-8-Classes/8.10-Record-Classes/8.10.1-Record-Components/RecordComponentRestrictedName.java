/*
 * @test
 * @summary JLS 8.10.1: it is a compile-time error for a record component to have
 *          one of the restricted names clone, finalize, getClass, hashCode,
 *          notify, notifyAll, toString, or wait.
 * @compile/fail RecordComponentRestrictedName.java
 */

record RecordComponentRestrictedName(int clone) {}   // error: illegal record component name
