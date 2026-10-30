/*
 * @test
 * @summary JLS 9.1.1.4: it is a compile-time error if an interface is declared
 *          non-sealed but has no sealed direct superinterface.
 * @compile/fail NonSealedWithoutSealedParent.java
 */

non-sealed interface NonSealedWithoutSealedParent {}   // error: no sealed superinterface
