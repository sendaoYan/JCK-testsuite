/*
 * @test
 * @summary JLS 7.6: a top level class or interface declaration must not contain
 *          the access modifier protected.
 * @compile/fail TopLevelProtectedModifier.java
 */

protected class TopLevelProtectedModifier {   // error: modifier protected not allowed
}
