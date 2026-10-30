/*
 * @test
 * @summary JLS 9.6.3 (Example 9.6.3-1): it is a compile-time error if Repeatable
 *          names a type that is not a containing annotation interface.
 * @compile/fail IllFormedContainingAnnotation.java
 */

import java.lang.annotation.Repeatable;

@Repeatable(FooContainer.class)
@interface Foo {}

@interface FooContainer { Object[] value(); }   // error: not a valid container for Foo
