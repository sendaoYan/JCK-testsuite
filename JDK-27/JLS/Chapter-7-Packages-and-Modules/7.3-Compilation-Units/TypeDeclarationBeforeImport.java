/*
 * @test
 * @summary JLS 7.3: import declarations must precede top level type
 *          declarations; a type declaration before an import declaration is a
 *          compile-time error (class, interface, or enum expected).
 * @compile/fail TypeDeclarationBeforeImport.java
 */

class First {}

import java.util.Vector;
