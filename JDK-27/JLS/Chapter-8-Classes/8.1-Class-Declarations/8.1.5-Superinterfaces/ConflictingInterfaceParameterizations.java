/*
 * @test
 * @summary JLS 8.1.5: a class may not declare supertypes that are different
 *          parameterizations of the same generic interface.
 * @compile/fail ConflictingInterfaceParameterizations.java
 */

interface I<T> {}

class B implements I<Integer> {}

class ConflictingInterfaceParameterizations extends B implements I<String> {}   // error
