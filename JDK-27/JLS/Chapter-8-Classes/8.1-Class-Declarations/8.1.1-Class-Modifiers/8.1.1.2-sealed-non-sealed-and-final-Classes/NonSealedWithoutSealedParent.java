/*
 * @test
 * @summary JLS 8.1.1.2: it is a compile-time error if a class is declared
 *          non-sealed but has neither a sealed direct superclass nor a sealed
 *          direct superinterface.
 * @compile/fail NonSealedWithoutSealedParent.java
 */

non-sealed class NonSealedWithoutSealedParent {}   // error: has no sealed parent
