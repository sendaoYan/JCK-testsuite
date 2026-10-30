/*
 * @test
 * @summary JLS 9.6.4.4: Override applied to a method that does not override or
 *          implement any supertype method is a compile-time error.
 * @compile/fail OverrideAnnotationWrong.java
 */

class OverrideAnnotationWrong {
    @Override void notOverride() {}   // error: method does not override a supertype method
}
