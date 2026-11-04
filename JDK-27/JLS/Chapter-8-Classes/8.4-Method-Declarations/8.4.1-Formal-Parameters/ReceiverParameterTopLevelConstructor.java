/*
 * @test
 * @summary JLS 8.4.1: a receiver parameter may not appear in the constructor of
 *          a top level class.
 * @compile/fail ReceiverParameterTopLevelConstructor.java
 */
class ReceiverParameterTopLevelConstructor {
    ReceiverParameterTopLevelConstructor(ReceiverParameterTopLevelConstructor this) { }   // error
}
