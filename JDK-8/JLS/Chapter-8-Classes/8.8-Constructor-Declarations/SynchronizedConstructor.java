/*
 * @test
 * @summary JLS 8.8.3 (Java SE 8): a constructor cannot be synchronized.
 * @compile/fail SynchronizedConstructor.java
 */

class SynchronizedConstructor {
    synchronized SynchronizedConstructor() {}   // error: modifier synchronized not allowed here
}
