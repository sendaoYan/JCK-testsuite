/*
 * @test
 * @summary JLS 8.4.1 (Java SE 8): a receiver parameter may appear only in an instance
 *          method; a static method may not have one.
 * @compile/fail ReceiverParameterStaticMethod.java
 */
class ReceiverParameterStaticMethod {
    static void m(ReceiverParameterStaticMethod this) { }   // error: this in static context
}
