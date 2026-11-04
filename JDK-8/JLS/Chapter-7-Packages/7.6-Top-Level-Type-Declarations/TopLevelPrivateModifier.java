/*
 * @test
 * @summary JLS 7.6 (Java SE 8): a top level type declaration may not contain the
 *          private access modifier.
 * @compile/fail TopLevelPrivateModifier.java
 */

private class TopLevelPrivateModifier {}
