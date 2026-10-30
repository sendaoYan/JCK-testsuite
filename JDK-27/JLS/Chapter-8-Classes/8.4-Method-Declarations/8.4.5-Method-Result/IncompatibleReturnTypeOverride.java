/*
 * @test
 * @summary JLS 8.4.5: if a method overrides a superclass method, the return
 *          type must be return-type-substitutable for the overridden method.
 * @compile/fail IncompatibleReturnTypeOverride.java
 */

class Point {
    int x;
    int getX() { return x; }
}

class RealPoint extends Point {
    float xr;
    float getX() { return xr; }   // error: incompatible return type
}
