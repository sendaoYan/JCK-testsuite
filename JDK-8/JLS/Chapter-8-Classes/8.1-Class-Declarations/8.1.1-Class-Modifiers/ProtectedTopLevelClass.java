/*
 * @test
 * @summary JLS 8.1.1 (Java SE 8): the access modifiers protected and private pertain only
 *          to member classes; a top level class may not be declared protected.
 * @compile/fail ProtectedTopLevelClass.java
 */
protected class ProtectedTopLevelClass { }   // error: modifier protected not allowed here
