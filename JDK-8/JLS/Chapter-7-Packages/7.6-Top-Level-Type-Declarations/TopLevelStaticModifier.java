/*
 * @test
 * @summary JLS 7.6 (Java SE 8): a top level type declaration may not contain the
 *          static modifier.
 * @compile/fail TopLevelStaticModifier.java
 */

static class TopLevelStaticModifier {}
