/*
 * @test
 * @summary JLS 4.5: "Pair<String> is illegal, as there are not enough type
 *          arguments." The number of type arguments must match the number of
 *          type parameters.
 * @compile/fail TooFewTypeArguments.java
 */

class Pair<X, Y> {}

class TooFewTypeArguments {
    Pair<String> p;   // error: wrong number of type arguments
}
