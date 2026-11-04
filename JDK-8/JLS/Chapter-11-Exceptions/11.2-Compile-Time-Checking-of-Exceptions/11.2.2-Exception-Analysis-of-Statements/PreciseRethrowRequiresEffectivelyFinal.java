/*
 * @test
 * @summary JLS 11.2.2 (Java SE 8): precise rethrow applies only to a final or
 *          effectively final catch parameter; assigning to it disables precise
 *          rethrow and the rethrow can throw the parameter's static type.
 * @compile/fail PreciseRethrowRequiresEffectivelyFinal.java
 */

import java.io.FileNotFoundException;

class PreciseRethrowRequiresEffectivelyFinal {
    void m() throws FileNotFoundException {
        try {
            throw new FileNotFoundException();
        } catch (Exception e) {
            e = new Exception();   // not effectively final
            throw e;               // error: unreported exception Exception
        }
    }
}
