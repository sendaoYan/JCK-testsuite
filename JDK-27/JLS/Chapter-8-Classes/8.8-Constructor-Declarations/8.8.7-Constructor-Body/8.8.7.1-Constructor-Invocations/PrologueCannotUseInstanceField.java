/*
 * @test
 * @summary JLS 8.8.7.1: a prologue preceding a constructor invocation occurs in
 *          an early construction context, so it must not refer to instance
 *          variables of the class being constructed.
 * @compile/fail PrologueCannotUseInstanceField.java
 */

class Super {
    Super(int x) {}
}

class PrologueCannotUseInstanceField extends Super {
    int field;
    PrologueCannotUseInstanceField() {
        int y = field;   // error: cannot reference field before supertype constructor
        super(1);
    }
}
