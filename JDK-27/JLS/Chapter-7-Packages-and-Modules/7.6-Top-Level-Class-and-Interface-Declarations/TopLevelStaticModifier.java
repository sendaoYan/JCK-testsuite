/*
 * @test
 * @summary JLS 7.6: a top level class or interface declaration must not contain
 *          the modifier static.
 * @compile/fail TopLevelStaticModifier.java
 */

static class TopLevelStaticModifier {   // error: modifier static not allowed
}
