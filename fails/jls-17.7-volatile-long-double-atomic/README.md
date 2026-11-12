# JLS 17.7 — volatile long/double 撕裂读（aarch64 C2 疑似原子性违规，跨两代 JDK 稳定复现）

## 结论

**判定为真实 C2 原子性违规，不弱化测试，仅记录。本轮最值得追的一条。**

`VolatileLongDoubleAtomic` 在 **aarch64（xuzhu）** 的 C2 编译配置（`-Xcomp`、
`xcomp_tiered4`）下确定性失败，断言 `observed a torn volatile long/double`：reader 读到了
既非 `LONG_A` 也非 `LONG_B`（/ `DOUBLE_A` / `DOUBLE_B`）的混合高低 32 位值。JLS 17.7 明文
保证 volatile long/double 读写是原子的，此测试编码的正是这条硬保证，因此**不改断言**。

**关键判别（2026-11-12 确认）**：JDK-8 与 JDK-27 **两代**都在 `xcomp` 与 `xcomp_tiered4`
（C2 全编译）下复现同一条撕裂读，而 `baseline` / GC（`g1`/`serial`）/ C1（`xcomp_notiered`、
`xcomp_tiered1`）/ `nobgcomp` 全都不复现 → 候选 **C2 破坏 JLS 17.7 volatile 原子性**，
跨两代 JDK 稳定复现，是真实 JIT 发散而非测试竞态或环境噪声。

## 测试环境

| 项 | 值 |
|---|---|
| 机器 | `xuzhu`（ssh xuzhu，Linux aarch64，320 CPU / 1 TB RAM） |
| 被测 JDK-27 | Temurin `jdk-27.0.1.0.2+1` aarch64（与 hanqinhu x86 同 build，仅换 arch） |
| 被测 JDK-8 | Temurin `jdk8u502-b07` aarch64 |
| jtreg | `8.3`（`/data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg`） |
| JTREG_JAVA | Temurin `jdk-17.0.20+8`（harness JVM） |
| 用例路径 | `JDK-{8,27}/JLS/Chapter-17-Threads-and-Locks/17.7-Non-Atomic-Treatment-of-double-and-long/VolatileLongDoubleAtomic.java` |

## 复现命令

```bash
# 在 xuzhu（aarch64）上
export JTREG_JAVA=/data/yansendao/JDK/binary/temurin/jdk-17.0.20+8
JTREG=/data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg
JDK27=/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1
JDK8=/data/yansendao/JDK/binary/temurin/jdk8u502-b07
T27=JDK-27/JLS/Chapter-17-Threads-and-Locks/17.7-Non-Atomic-Treatment-of-double-and-long/VolatileLongDoubleAtomic.java
T8=JDK-8/JLS/Chapter-17-Threads-and-Locks/17.7-Non-Atomic-Treatment-of-double-and-long/VolatileLongDoubleAtomic.java

# 复现（C2 路径，JDK-8 与 JDK-27 两代都失败）：
"$JTREG" -jdk:"$JDK27" -javaoption:-Xcomp "$T27"
"$JTREG" -jdk:"$JDK27" -javaoption:-Xcomp -javaoption:-XX:TieredStopAtLevel=4 "$T27"
"$JTREG" -jdk:"$JDK8"  -javaoption:-Xcomp "$T8"
"$JTREG" -jdk:"$JDK8"  -javaoption:-Xcomp -javaoption:-XX:TieredStopAtLevel=4 "$T8"

# 不复现（解释器 / C1 / 关闭后台编译 路径）：
"$JTREG" -jdk:"$JDK27" -javaoption:-Xcomp -javaoption:-XX:-TieredCompilation "$T27"
"$JTREG" -jdk:"$JDK27" -javaoption:-Xint "$T27"
"$JTREG" -jdk:"$JDK27" -javaoption:-XX:-BackgroundCompilation "$T27"
```

## 测试结果

| 平台 | JDK | 配置 | 结果 |
|---|---|---|---|
| aarch64（xuzhu） | JDK-27 | `xcomp`（C2） | **失败** `observed a torn volatile long/double`（确定性） |
| aarch64（xuzhu） | JDK-27 | `xcomp_tiered4`（C2） | **失败**（同上） |
| aarch64（xuzhu） | JDK-8 | `xcomp`（C2） | **失败**（同上） |
| aarch64（xuzhu） | JDK-8 | `xcomp_tiered4`（C2） | **失败**（同上） |
| aarch64（xuzhu） | JDK-27 | `baseline` / `g1` / `serial`（GC） | 通过 |
| aarch64（xuzhu） | JDK-27 | `xcomp_notiered` / `xcomp_tiered1`（C1） | 通过 |
| aarch64（xuzhu） | JDK-27 | `nobgcomp`（`-XX:-BackgroundCompilation`） | 通过 |
| aarch64（xuzhu） | JDK-27 | `-Xint`（纯解释） | 通过 |
| x86（hanqinhu） | JDK-8 / JDK-27 | 全部 10 配置 | 通过（无一失败） |

> 判定依据：只有到达 C2 的配置（`xcomp`、`xcomp_tiered4`）复现，且 JDK-8 / JDK-27 两代
> 表现一致 → 候选 C2 在 aarch64 上把某条 volatile 64 位读拆成两个 32 位 load 的代码生成问题。

## 根因分析

- 仅在 C2 编译路径复现；解释器与 C1 不失败 → 不是测试竞态，而是 C2 在 aarch64 上把某条
  volatile 64 位读**拆成两个 32 位 load** 的代码生成问题（撕裂读）。
- 测试本身：writer 交替写 `0x00000000FFFFFFFF` / `0xFFFFFFFF00000000`，reader 断言读到的
  值要么高 32 位全 0、要么高 32 位全 1；观察到混合值即证明两个 32 位 load 之间插入了 writer
  的一次 store，违反了 JLS 17.7 的 volatile 原子性保证。
- 该保证是 JLS 硬性语义，不是"提示"，因此不得用放宽断言来回避。

## 处置

- **不弱化测试**：`JDK-{8,27}` 树内的 `VolatileLongDoubleAtomic.java` 保持严格断言
  （任何既非 A 也非 B 的值即 tear）。
- 在 aarch64 `-Xcomp`/`xcomp_tiered4` sweep 中报告此失败时，归为**真实平台发散**，
  与 [[../README.md]] 记录的其它 aarch64 结构断言发散同类，记录不修。
- 本目录留归档副本 `VolatileLongDoubleAtomic.java` + 本文档，便于复现与上游反馈。
