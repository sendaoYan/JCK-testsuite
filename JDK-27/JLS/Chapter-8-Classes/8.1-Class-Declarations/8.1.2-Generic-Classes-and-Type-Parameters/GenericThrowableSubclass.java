/*
 * @test
 * @summary JLS 8.1.2: it is a compile-time error if a generic class is a direct
 *          or indirect subclass of Throwable.
 * @compile/fail GenericThrowableSubclass.java
 */

class GenericThrowableSubclass<T> extends Throwable {}   // error: generic class may not extend Throwable
