# JLS 17.7 — volatile long/double 撕裂读（原判“C2 原子性违规”，已纠正为测试误报）

## 结论（2026-11-12 纠正，推翻原判定）

**这不是 C2 原子性违规，是测试误报（false positive）。** 原“真实 C2 把 volatile 64 位读拆成两个
32 位 load”的判定**不成立**，已撤销。JLS 17.7 的 volatile long/double 原子性保证仍是硬性语义，
本测试断言保持严格（任何既非 A 也非 B 的值即 tear），下述修复只消除误报、补全 double 侧检测能力，
不弱化断言。

### 误报机制

reader 在 writer 首次 store 之前读到了字段默认值 `0`。而 `LONG_A = 0x00000000FFFFFFFF`、
`LONG_B = 0xFFFFFFFF00000000` 的“混合高低 32 位”取值之一恰是 `0x0000000000000000`（= 默认 0），
于是“读默认 0”与“真实撕裂读到 0”无法区分，reader 把默认 0 当成 tear 而失败。

为什么只在 C2 配置（`-Xcomp` / `xcomp_tiered4`）复现：`-Xcomp` 下 writer 启动即被编译、首次
store 相对更晚，reader 更早抢到默认 0；解释器 / C1 / `nobgcomp` 时序不同故不触发。这是**时序巧合**，
不是代码生成缺陷——原“只有 C2 复现 ⇒ C2 拆 64 位 load”的因果推断不成立。

### 两处修复

| # | 修复 | 状态 |
|---|---|---|
| 1 | 启线程前先 `lv = LONG_A; dv = DOUBLE_A;` 种子，杜绝“读默认值”误报源 | commit `8af14bc84`，已上线 |
| 2 | double 常数由低 32 位同为 `0xFFFFFFFF` 的 `0x00000000FFFFFFFF` / `0x80000000FFFFFFFF`（任何混合都坍缩回 A/B，检测形同虚设）改为高低 32 位均不同的非 NaN 常量 `0x3FF00000FFFFFFFF` / `0x4000000000000000` | 本会话修复，已上线 |

## 复现命令（仅对修复前快照有效）

下列命令在**修复前的未种子 + 退化常数版本**（`git show 8af14bc84^`）上可复现
`observed a torn volatile long/double`；对当前（已种子 + 常数已修）版本，这些命令应**全部 PASS**。

```bash
# 在 xuzhu（aarch64）上
export JTREG_JAVA=/data/yansendao/JDK/binary/temurin/jdk-17.0.20+8
JTREG=/data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg
JDK27=/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1
JDK8=/data/yansendao/JDK/binary/temurin/jdk8u502-b07
T27=JDK-27/JLS/Chapter-17-Threads-and-Locks/17.7-Non-Atomic-Treatment-of-double-and-long/VolatileLongDoubleAtomic.java
T8=JDK-8/JLS/Chapter-17-Threads-and-Locks/17.7-Non-Atomic-Treatment-of-double-and-long/VolatileLongDoubleAtomic.java

"$JTREG" -jdk:"$JDK27" -javaoption:-Xcomp "$T27"
"$JTREG" -jdk:"$JDK27" -javaoption:-Xcomp -javaoption:-XX:TieredStopAtLevel=4 "$T27"
"$JTREG" -jdk:"$JDK8"  -javaoption:-Xcomp "$T8"
"$JTREG" -jdk:"$JDK8"  -javaoption:-Xcomp -javaoption:-XX:TieredStopAtLevel=4 "$T8"
```

## 历史结果矩阵（修复前，仅供参考）

| 平台 | JDK | 配置 | 结果 |
|---|---|---|---|
| aarch64（xuzhu） | JDK-27 | `xcomp`（C2） | 误报失败（读默认 0） |
| aarch64（xuzhu） | JDK-27 | `xcomp_tiered4`（C2） | 误报失败（同上） |
| aarch64（xuzhu） | JDK-8 | `xcomp` / `xcomp_tiered4`（C2） | 误报失败（同上） |
| aarch64（xuzhu） | JDK-27 | `baseline` / `g1` / `serial` / `xcomp_notiered` / `xcomp_tiered1` / `nobgcomp` / `-Xint` | 通过 |
| x86（hanqinhu） | JDK-8 / JDK-27 | 全部 10 配置 | 通过 |

> 原判定“只有 C2 复现 ⇒ C2 拆 64 位 load”错在：把“更易触发读默认 0 的时序”当成了“代码生成缺陷”。
