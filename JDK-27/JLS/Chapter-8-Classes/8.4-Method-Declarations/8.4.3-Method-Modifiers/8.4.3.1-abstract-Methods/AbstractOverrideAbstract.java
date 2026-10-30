/*
 * @test
 * @summary JLS 8.4.3.1: an abstract method can be overridden by another abstract
 *          method (refining the throws clause), and a non-abstract subclass must
 *          provide a concrete implementation.
 * @run main AbstractOverrideAbstract
 */

class BufferError extends Exception {}
class BufferEmpty extends Exception {}

interface Buffer {
    char get() throws BufferEmpty, BufferError;
}

abstract class InfiniteBuffer implements Buffer {
    // refines the throws clause of get(): never throws BufferEmpty
    public abstract char get() throws BufferError;
}

class ConcreteBuffer extends InfiniteBuffer {
    public char get() { return 'x'; }
}

public class AbstractOverrideAbstract {
    public static void main(String[] args) {
        Buffer b = new ConcreteBuffer();
        try {
            if (b.get() != 'x') throw new AssertionError("get()");
        } catch (Exception e) {
            throw new AssertionError("unexpected: " + e);
        }
        System.out.println("PASS");
    }
}
