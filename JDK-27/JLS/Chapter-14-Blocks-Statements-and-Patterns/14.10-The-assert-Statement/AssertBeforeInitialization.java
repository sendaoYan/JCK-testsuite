/*
 * @test
 * @summary JLS 14.10: an assert statement executed before its class has completed
 *          initialization is enabled, so in a circular static initialization the
 *          first (reentrant) evaluation reports enabled and the post-init one
 *          reports disabled.
 * @run main AssertBeforeInitialization
 */

public class AssertBeforeInitialization {
    static final StringBuilder log = new StringBuilder();

    public static void main(String[] args) {
        Child.probe();
        if (!"on,off,".equals(log.toString()))
            throw new AssertionError("expected on,off, got " + log);
        System.out.println("PASS");
    }
}

class Parent {
    static {
        Child.probe();
    }
}

class Child extends Parent {
    static void probe() {
        boolean on = false;
        assert on = true;
        AssertBeforeInitialization.log.append(on ? "on," : "off,");
    }
}
