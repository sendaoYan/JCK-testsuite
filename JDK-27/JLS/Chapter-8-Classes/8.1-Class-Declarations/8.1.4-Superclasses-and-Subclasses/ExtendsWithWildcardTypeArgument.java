/*
 * @test
 * @summary JLS 8.1.4: if the ClassType in an extends clause has type arguments,
 *          none may be wildcard type arguments.
 * @compile/fail ExtendsWithWildcardTypeArgument.java
 */
class Y<T> { }
class ExtendsWithWildcardTypeArgument extends Y<?> { }   // error: unexpected type (wildcard)
