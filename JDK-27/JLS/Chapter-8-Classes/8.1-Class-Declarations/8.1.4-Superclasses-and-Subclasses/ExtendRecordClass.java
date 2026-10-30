/*
 * @test
 * @summary JLS 8.1.4: the class Record can only be extended by a record class;
 *          extending it from a normal class is a compile-time error.
 * @compile/fail ExtendRecordClass.java
 */

class ExtendRecordClass extends Record {}   // error: cannot extend Record
