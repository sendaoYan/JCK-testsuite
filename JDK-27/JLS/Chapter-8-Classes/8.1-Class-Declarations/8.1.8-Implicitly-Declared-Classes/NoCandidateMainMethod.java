/*
 * @test
 * @summary JLS 8.1.8: it is a compile-time error if the class implicitly
 *          declared by a compact compilation unit does not declare at least one
 *          candidate main method.
 * @compile/fail NoCandidateMainMethod.java
 */

int field = 1;
