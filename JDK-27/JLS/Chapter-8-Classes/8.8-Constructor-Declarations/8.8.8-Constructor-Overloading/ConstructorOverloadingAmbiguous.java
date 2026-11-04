/*
 * @test
 * @summary JLS 8.8.8: a class instance creation expression may be ambiguous when
 *          multiple constructors are applicable by method invocation conversion.
 * @compile/fail ConstructorOverloadingAmbiguous.java
 */

class ConstructorOverloadingAmbiguous {
    ConstructorOverloadingAmbiguous(int i, long l) {}
    ConstructorOverloadingAmbiguous(long l, int i) {}

    ConstructorOverloadingAmbiguous t = new ConstructorOverloadingAmbiguous(1, 1);
    // error: reference to ConstructorOverloadingAmbiguous is ambiguous
}
