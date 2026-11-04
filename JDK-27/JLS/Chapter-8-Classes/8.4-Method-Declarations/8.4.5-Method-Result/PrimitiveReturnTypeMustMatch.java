/*
 * @test
 * @summary JLS 8.4.5 / 8.4.8.3: an overriding method with a primitive return
 *          type must have exactly the same return type as the overridden method.
 * @compile/fail PrimitiveReturnTypeMustMatch.java
 */
class Point {
    int getX() { return 1; }
}
class PrimitiveReturnTypeMustMatch extends Point {
    float getX() { return 1.0f; }   // error: return type float is not compatible with int
}
