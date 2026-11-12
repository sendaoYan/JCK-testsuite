# 上游确认邮件草稿（OpenJDK hotspot-dev）

> 状态：**未发送**。本目录的取证已能自证结论，发信仅作为外部二次确认的可选项。
> 发送前需人工确认收件列表与措辞；不要自动发送。

---

**To:** hotspot-dev@openjdk.org
**Subject:** [FYI] AArch64 `-Xcomp` "torn volatile long" report traced to a pre-store default read, not a C2 atomicity bug

Hi all,

We maintain a jtreg conformance suite and hit a failure worth reporting only
because it looked like an AArch64 C2 atomicity violation, and turned out not to
be one. Posting the analysis so nobody else spends time on the same false alarm.

**Symptom.** A JLS 17.7 test (`volatile long`/`double` reads must never observe a
mixed 32-bit halves value) failed *only* under `-Xcomp` and
`-Xcomp -XX:TieredStopAtLevel=4`, on AArch64, with both JDK 8u and JDK 27. All
other configs (`-Xint`, tiered, G1/Serial, `-Xcomp -XX:-TieredCompilation` on x86)
passed. The natural reading was "C2 splits a 64-bit volatile load on AArch64".

**Why that reading was wrong.** The test looked like this (abridged):

```java
static volatile long lv;
static final long A = 0x00000000FFFFFFFFL;   // high=0x00000000 low=0xFFFFFFFF
static final long B = 0xFFFFFFFF00000000L;   // high=0xFFFFFFFF low=0x00000000
// ... reader:
long v = lv;
if (v != A && v != B) fail("torn");
```

The two mixed-halves values are `0x0000000000000000` (= `A.high | B.low`) and
`0xFFFFFFFFFFFFFFFF`. The first one is *exactly the field's default value*. The
reader was started without seeding `lv`/`dv` first, so any reader iteration that
ran before the writer's first store read `0` and reported a "tear". The window is
timing-dependent, which is why `-Xcomp` (slower start, more compile-thread
contention) made it frequent and why pinning to one core made it reproducible
11/40 on JDK 27.

**Minimal reproduction of the false positive** (complete file, no dependencies):

```java
public class Probe {
    static volatile long lv;
    static volatile boolean firstStoreDone;
    static final long A = 0x00000000FFFFFFFFL;
    static final long B = 0xFFFFFFFF00000000L;

    public static void main(String[] x) throws Exception {
        Thread w = new Thread(() -> {          // deliberately unseeded, as in the report
            lv = A; firstStoreDone = true;
            for (;;) { lv = A; lv = B; }
        });
        long[] n = new long[1], first = new long[1];
        boolean[] pre = new boolean[1];
        Thread r = new Thread(() -> {
            for (;;) {
                long v = lv;
                if (v != A && v != B && n[0]++ == 0) {
                    first[0] = v; pre[0] = !firstStoreDone;
                }
            }
        });
        w.setDaemon(true); r.setDaemon(true); w.start(); r.start();
        Thread.sleep(3000);
        System.out.printf("unexpected=%d first=0x%016X beforeFirstStore=%b%n",
                          n[0], first[0], pre[0]);
        System.exit(0);
    }
}
```

Run it as `taskset -c 0 java -Xcomp Probe`. On AArch64 (Temurin 8u502-b07 and
27.0.1.0.2) it reports roughly 4 runs in 10, every value is
`0x0000000000000000`, all of them with `beforeFirstStore=true`, and the counter
is in the millions — the field can only hold its default at that instant, so the
reader simply got in before the writer thread's `run()` finished compiling (which
is exactly why `-Xcomp` makes this frequent).

**The real test now.** Two changes, neither of which weakens the assertion
(any value other than A or B is still a failure):

1. Seed `lv = A; dv = DA;` before starting the threads.
2. Pick constants whose four halves-combinations are pairwise distinct and none
   of which is `0` — e.g. `A = 0x1111111122222222L`, `B = 0x3333333344444444L`
   (mixed values `0x1111111144444444` and `0x3333333322222222`).

With that, on the same machine: `-Xcomp` ×100 and
`-Xcomp -XX:TieredStopAtLevel=4` ×60 across JDK 8u and JDK 27, plus single-core
pinned runs, total ≈ 1.8 × 10^10 volatile read pairs, zero tears. A deliberate
negative control (a writer that publishes a mixed value on purpose) is detected
immediately on both x86 and AArch64, so the detector is not vacuous.

Two side notes that may be useful to others writing such tests:

- A second real defect in the original test: on the `double` side both constants
  had the same low 32 bits, so every mixed value collapsed back to one of the two
  legal values and `double` tearing could never be detected at all. The test was
  passing vacuously on that half.
- We did check JDK-8394913 / JDK-8397181 (C2 losing memory ordering for unused
  volatile/acquire reads on AArch64). Not applicable here: our reads are used, and
  the reports are about ordering, not 64-bit tearing.

We are not filing a bug, since we believe there is none. We would be glad to be
told otherwise — in particular if anyone knows of an AArch64 C2 case where an
aligned `volatile long` load was actually emitted as two 32-bit loads.

Thanks,
Sendao Yan
