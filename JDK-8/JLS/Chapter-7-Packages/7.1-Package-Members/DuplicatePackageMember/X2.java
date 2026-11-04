/*
 * @test
 * @summary JLS 7.1 (Java SE 8): a package may not contain two members of the
 *          same name; two compilation units declaring the same top level class
 *          in one package is a compile-time error (duplicate class X).
 * @compile/fail X1.java X2.java
 */

package p;

class X {}
