/*
 * @test
 * @summary JLS 11.1.1: a subclass of Throwable must not be generic.
 * @compile/fail ThrowableCannotBeGeneric.java
 */

class ThrowableCannotBeGeneric<E> extends Throwable {   // error: generic class may not extend java.lang.Throwable
}
