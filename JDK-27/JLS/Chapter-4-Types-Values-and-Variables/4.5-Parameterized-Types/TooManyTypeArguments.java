/*
 * @test
 * @summary JLS 4.5: "Pair<String,String,String> is illegal, as there are too
 *          many type arguments." The number of type arguments must match the
 *          number of type parameters.
 * @compile/fail TooManyTypeArguments.java
 */

class Pair<X, Y> {}

class TooManyTypeArguments {
    Pair<String, String, String> p;   // error: wrong number of type arguments
}
