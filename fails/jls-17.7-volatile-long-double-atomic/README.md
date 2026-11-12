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

为什么历史上只在 C2 配置（`-Xcomp` / `xcomp_tiered4`）出现：这是 sweep 当时的线程调度时序结果，
不是 C2 的代码生成特性。`-Xcomp` 下 JVM 启动更慢、编译线程争用更多，reader 先于 writer 首次 store
运行的时间窗更容易被命中。实验（2026-11-12）显示：在单核争用（`taskset -c 0`）+ `-Xcomp` 下，
旧版可复现；同样的 `-Xcomp` 在机器空闲时 20/20 不复现。**关键是：复现时观察值恒为 `0x0`（默认值）**，
见下节。

### 2026-11-12 独立复现与验证（`repro/jls-17.7-volatile-atomicity/`）

为回答“这到底是不是 JVM bug”，构造了独立探针 `TornVolatileProbe`，把“读到默认值”与“真实撕裂”
在**数据上分开**：

| 实验 | 结果 |
|---|---|
| `legacy` 模式（旧测试逻辑）+ 单核争用 + `-Xcomp` | 3/10、3/6 复现历史失败，**观察值全部 `0x0000000000000000`**；其中 `beforeFirstStore=true` 的记录证明该值出现在 writer 首次 store 之前——那时字段只可能是默认 0，撕裂物理上不可能 |
| `no-seed` 模式（同样的时序，但不把 0 判成撕裂） | 同一争用条件下出现 `PRE_STORE>0` 且 `TORN=0`，即那些被旧测试判成“撕裂”的读就是默认值 0 |
| `strict` 模式（常量两两不同 + 启线程前种子） | aarch64 上 `-Xcomp` 100 次、`xcomp_tiered4` 60 次、默认 20 次、单核争用 6 次，共约 **180 亿次 volatile 读，`TORN=0`** |
| `selftest` 负向对照（writer 故意写入混合值） | x86/aarch64、JDK-8/JDK-27 均准确报出 TEAR，证明检测器不是“永远通过”的空壳 |
| 上游 issue 检索 | aarch64 C2 volatile 相关（JDK-8394913 / JDK-8397181）均为“未被使用”的 volatile/acquire 读丢失内存序，不涉及 64 位读撕裂 |

结论：**历史失败 = reader 在 writer 首次 store 前读到默认值 0**，与 JVM 代码生成无关；
`strict` 模式在约 180 亿次读中未观察到任何撕裂。

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
