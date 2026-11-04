/*
 * @test
 * @summary JLS 8.1.5: if an InterfaceType in an implements clause has type
 *          arguments, none may be wildcard type arguments.
 * @compile/fail ImplementsWithWildcardTypeArgument.java
 */
interface I<T> { }
class ImplementsWithWildcardTypeArgument implements I<?> { }   // error: unexpected type (wildcard)
