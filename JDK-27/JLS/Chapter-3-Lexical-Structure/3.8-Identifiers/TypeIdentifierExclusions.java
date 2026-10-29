/*
 * @test
 * @summary JLS 3.8: TypeIdentifier excludes permits, record, sealed, var, and
 *          yield, so none of those may be used as a class/interface/type name.
 * @compile/fail TypeIdentifierExclusions.java
 */

// Each of the following is a contextual keyword that is not a TypeIdentifier.
class record {}
class var {}
class yield {}
class sealed {}
class permits {}
