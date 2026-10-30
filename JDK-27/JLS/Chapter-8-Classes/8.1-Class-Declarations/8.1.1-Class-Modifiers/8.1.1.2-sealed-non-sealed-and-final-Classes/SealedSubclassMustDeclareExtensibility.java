/*
 * @test
 * @summary JLS 8.1.1.2: a class that has a sealed direct superclass must be
 *          declared final, sealed, or non-sealed (explicitly or implicitly);
 *          otherwise it is a compile-time error.
 * @compile/fail SealedSubclassMustDeclareExtensibility.java
 */

sealed class Base permits Good {}

final class Good extends Base {}

class SealedSubclassMustDeclareExtensibility extends Base {}   // error: must be final/sealed/non-sealed
