/*
 * @test
 * @summary JLS 7.6 (Java SE 8): a top level type declaration may not contain the
 *          protected access modifier.
 * @compile/fail TopLevelProtectedModifier.java
 */

protected class TopLevelProtectedModifier {}
