/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): the TypeName of a single-type-import
 *          declaration must be qualified; an unqualified simple name is a
 *          compile-time error ('.' expected).
 * @compile/fail SingleTypeImportUnqualified.java
 */

import Foo;

class T {}
