/*
 * @test
 * @summary JLS 15.8.4: a qualified this expression ClassName.this denotes the
 *          lexically enclosing instance of the named class, and lets a nested
 *          class access the enclosing instance even when it is hidden.
 * @run main QualifiedThis
 */

public class QualifiedThis {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    int value = 42;

    class Inner {
        QualifiedThis outer()       { return QualifiedThis.this; }
        int outerValue()            { return QualifiedThis.this.value; }
    }

    void testLocalClass() {
        class Local {
            QualifiedThis outer() { return QualifiedThis.this; }
        }
        check(new Local().outer() == QualifiedThis.this, "qualified this in local class");
    }

    public static void main(String[] args) {
        QualifiedThis o = new QualifiedThis();
        Inner i = o.new Inner();
        check(i.outer() == o, "Inner.outer() is the enclosing instance");
        check(i.outerValue() == 42, "qualified this reaches enclosing field");
        o.testLocalClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
