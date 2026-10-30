/*
 * @test
 * @summary JLS 8.8.7.1: a prologue preceding a constructor invocation occurs in
 *          an early construction context, so the keyword this is prohibited.
 * @compile/fail PrologueCannotUseThis.java
 */

class Super {
    Super(int x) {}
}

class PrologueCannotUseThis extends Super {
    PrologueCannotUseThis() {
        this.hashCode();   // error: cannot reference this before supertype constructor
        super(1);
    }
}
