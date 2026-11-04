/*
 * @test
 * @summary JLS 8.4.1 (Java SE 8): in an instance method the type of the receiver parameter
 *          must be the class in which the method is declared.
 * @compile/fail ReceiverParameterWrongType.java
 */
class ReceiverParameterWrongType {
    void m(Object this) { }   // error: receiver type does not match enclosing class type
}
