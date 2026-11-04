/*
 * @test
 * @summary JLS 7.3 (Java SE 8): the grammar of a compilation unit is
 *          [PackageDeclaration] {ImportDeclaration} {TypeDeclaration}; an import
 *          declaration preceding the package declaration is a compile-time error
 *          (class, interface, or enum expected).
 * @compile/fail ImportBeforePackageDeclaration.java
 */

import java.util.Vector;

package p;
