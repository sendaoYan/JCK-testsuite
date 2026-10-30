/*
 * @test
 * @summary JLS 7.6 (Java SE 8): a compilation unit may contain at most one public
 *          top-level type.
 * @compile/fail TwoPublicTypes.java
 */

public class TwoPublicTypes {}

public class AlsoPublic {}   // error: class AlsoPublic is public, should be in its own file
