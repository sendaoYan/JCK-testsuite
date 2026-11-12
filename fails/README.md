# fails/ — 已知平台/JIT 发散记录

本目录收集 **判定为真实 JDK 发散（非测试缺陷、非环境伪象）、但按测试规范不应弱化断言**
的用例。原用例仍保留在 `JDK-{8,27}/` 树内并照常参与 sweep（强断言不弱化）；这里放一份
归档副本 + 逐条记录文档，供复现、归因、上游反馈。

| 目录 | 用例 | 平台/JIT | 判定 |
|---|---|---|---|
| [jls-17.7-volatile-long-double-atomic/](jls-17.7-volatile-long-double-atomic/) | JLS 17.7 `VolatileLongDoubleAtomic` | aarch64 C2（`-Xcomp` / `xcomp_tiered4`），JDK-8 与 JDK-27 | **测试误报**（reader 读默认 0），已修复：种子（`8af14bc84`）+ double 常数修正。非真实 C2 缺陷 |

> 注：本表仅收“真实 JDK 发散”。上条经 2026-11-12 复查推翻为测试误报，保留作历史记录，不再按平台发散处置。
