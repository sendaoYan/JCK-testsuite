/*
 * @test
 * @summary JLS 7.6: a top level class or interface declaration must not contain
 *          the access modifier private.
 * @compile/fail TopLevelPrivateModifier.java
 */

private class TopLevelPrivateModifier {   // error: modifier private not allowed
}
