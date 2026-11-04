/*
 * @test
 * @summary JLS 8.1.1 (Java SE 8): the access modifiers protected and private pertain only
 *          to member classes; a top level class may not be declared private.
 * @compile/fail PrivateTopLevelClass.java
 */
private class PrivateTopLevelClass { }   // error: modifier private not allowed here
