/*
 * @test
 * @summary JLS 8.1.1.2: it is a compile-time error if a class is declared both
 *          final and abstract, because such a class could never be completed.
 * @compile/fail AbstractFinalClass.java
 */

abstract final class AbstractFinalClass {   // error: illegal combination
}
