/*
 * @test
 * @summary JLS 4.5: "Seq<int> is illegal, as primitive types cannot be type
 *          arguments." A primitive type may not be used as a type argument.
 * @compile/fail PrimitiveTypeArgument.java
 */

class Seq<T> {}

class PrimitiveTypeArgument {
    Seq<int> s;   // error: int is not a valid type argument
}
