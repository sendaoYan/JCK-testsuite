# JLS 17.7 volatile long/double 原子性 —— 最小可复现例子

本目录有三个 Java 文件（都无外部依赖）：

| 文件 | 用途 |
|---|---|
| `MinimalFalsePositive.java` | **约 100 行的精简复现例**，`legacy` / `fixed` 两种模式直接对照。想快速眼见为实就看这个 |
| `TornVolatileProbe.java` | 取证工具：`strict` / `no-seed` / `legacy` / `selftest` 四模式，带详细统计与负向自检 |
| `UPSTREAM-REPORT.md` | 给 hotspot-dev 的上游确认邮件草稿（**未发送**） |

两个程序都用于**独立复现**并**区分**下面两种现象：

| 现象 | 含义 |
|---|---|
| **真实撕裂读（TEAR）** | reader 读到由两个 32 位半字拼成的混合值 → 违反 JLS 17.7，是 JVM/JIT 缺陷 |
| **读到默认值 0（PRE-STORE）** | reader 在 writer 首次 store 之前就读了字段，读到类字段默认值 0 → 测试误报，不是 JVM 缺陷 |

历史报告的失败**无法区分**这两者，因为旧测试用的两个常量恰好让“混合值之一”等于 `0`：

```
LONG_A = 0x00000000FFFFFFFF   (high=0x00000000, low=0xFFFFFFFF)
LONG_B = 0xFFFFFFFF00000000   (high=0xFFFFFFFF, low=0x00000000)
两种混合值 = A.high||B.low = 0x0000000000000000   ← 与默认值 0 相同
             B.high||A.low = 0xFFFFFFFFFFFFFFFF
```

`strict` 模式把常量换成四个两两不同且都非 0 的值，并在起线程前写入合法种子，从而
**彻底消除**“默认值 0”这条误报路径；若 `strict` 仍报 TEAR，那就是真 bug。

---

## 精简版：`MinimalFalsePositive.java`（约 100 行，推荐第一次就用手跑）

`TornVolatileProbe.java` 是取证工具（多模式、详细报告）。如果只想**眼见为实**地看到
“历史失败其实就是读默认 0”，用这个精简版，两个模式直接对照：

```bash
cd repro/jls-17.7-volatile-atomicity
javac -d out MinimalFalsePositive.java          # 用 javac 8 及以上均可

# A. 旧逻辑（归档测试的常数 + 不起线程前种子）：可能报 UNEXPECTED，值恒为 0x0
taskset -c 0 java -Xcomp -cp out MinimalFalsePositive legacy 5000

# B. 修正后（四种半字组合两两不同 + 种子）：永不报 UNEXPECTED
taskset -c 0 java -Xcomp -cp out MinimalFalsePositive fixed 5000
```

`taskset -c 0`（把两个线程挤到一个核上）不是必须的，但能显著提高复现率——它放大的是
“reader 抢在 writer 第一次 store 之前运行”的时间窗。退出码：0 = 什么都没观察到，
2 = 观察到既非 A 也非 B 的值。

> `legacy` 的复现率依机器/核数/JDK 而变：xuzhu(aarch64) 上 `TornVolatileProbe legacy` 为
> JDK-27 11/40、JDK-8 0/40，本精简版 `legacy` 为 JDK-27 4/10；x86 上很少见。
> **复现不出来也不影响结论**——`fixed` 模式在同样条件下永不报错，才是要证明的事。

---

## 复现步骤

### 1. 编译

```bash
cd repro/jls-17.7-volatile-atomicity
javac -d out TornVolatileProbe.java
```

### 2. 五个模式

```bash
# A. 严格模式：能真正证明撕裂（seeded + 常量两两不同）
java -Xcomp -cp out TornVolatileProbe strict 3000

# B. 无种子模式：证明“reader 可能早于 writer 首次 store”这条时间窗确实存在
java -Xcomp -cp out TornVolatileProbe no-seed 3000

# B'. 同上，但让 writer 延迟 500ms 启动 → PRE_STORE 计数必然 > 0（确定性演示时间窗）
java -Xcomp -cp out TornVolatileProbe no-seed 3000 500

# C. 旧版复现：完全照搬归档测试的常量与“不设种子”，复现历史失败
java -Xcomp -cp out TornVolatileProbe legacy 3000

# D. 负向自检：writer 故意写入一个混合值并保持 50ms，检测器必须报 TEAR-DETECTED
java -Xcomp -cp out TornVolatileProbe selftest 1500
```

参数：`<mode> [durationMs=3000] [writerDelayMs=0]`

`selftest` 是**检测器有效性证明**：它必须输出 `RESULT: TEAR-DETECTED`。如果 `selftest`
都报不出撕裂，说明检测器本身失效（这正是旧测试 double 侧的缺陷——常量退化导致永远检不出）。

### 3. 判定

| 输出 | 退出码 | 结论 |
|---|---|---|
| `RESULT: OK — no tear observed` | 0 | 未观察到撕裂 |
| `RESULT: OK ... (but N PRE-STORE default reads)` | 0 | 存在“读到默认值”的时间窗，但没有撕裂 |
| `RESULT: LEGACY-FAIL ... before the writer completed any store` | 2 | **观察值出现在 writer 首次 store 之前，只能是默认 0，不是撕裂** |
| `RESULT: LEGACY-FAIL observed value 0x0000000000000000 == default` | 2 | **旧测试的失败 = 读到默认值 0，不是真撕裂** |
| `RESULT: LEGACY-FAIL observed a non-zero unexpected value` | 2 | 需进一步分析 |
| `RESULT: TEAR-DETECTED — genuine JLS 17.7 violation` | 2 | **真实 JLS 17.7 违反，应上报** |

输出里的 `beforeFirstStore=true` 表示记录异常值时的另一次 volatile 读也看到 writer 尚未完成
任何 store——该时刻字段只可能是默认 0，因此**确定性排除撕裂**。（`false` 表示两次读之间 flag
可能刚翻转，不作定论；以 `strict` 模式准。）

---

## 建议的验证矩阵

```bash
for mode in strict no-seed legacy selftest; do
  for i in $(seq 1 50); do
    java -Xcomp -cp out TornVolatileProbe $mode 2000 > /tmp/probe.$mode.$i.log 2>&1
    echo "$mode run$i rc=$? $(grep -o 'RESULT:.*' /tmp/probe.$mode.$i.log)"
  done
done
```

再换 `-Xcomp -XX:TieredStopAtLevel=4`、`-Xint`、默认（分层）各跑一轮做对照。
在**单核争用**（`taskset -c 0`）下最容易复现“读到默认值”的时间窗。

---

## 实测结果（2026-11-12，xuzhu aarch64 / Temurin JDK-8u502 + JDK-27.0.1.0.2，`-Xcomp`）

### 严格模式：未观察到任何撕裂

| 配置 | JDK-8 | JDK-27 | 结果 |
|---|---|---|---|
| `-Xcomp` | 50 次 | 50 次 | 100/100 OK |
| `-Xcomp -XX:TieredStopAtLevel=4` | 30 次 | 30 次 | 60/60 OK |
| 默认分层编译 | 10 次 | 10 次 | 20/20 OK |
| `-Xcomp` + `taskset -c 0` | 40 次 | 40 次 | 80/80 OK |

每轮约 1 亿次 volatile long + double 读对；加上 80 轮单核争用，累计约 **250 亿次读**，`TORN=0`。

### 负向自检：检测器有效

`selftest` 在 x86 / aarch64、JDK-8 / JDK-27 上均准确报出注入的混合值
（long `0x1111111144444444`、double `0x3FF0000077778888`）。

### 精简版 `MinimalFalsePositive` 实测（xuzhu aarch64 / JDK-27，`taskset -c 1 -Xcomp`，各 10 轮）

| 模式 | 结果 |
|---|---|
| `legacy` | **4/10 报 UNEXPECTED**，值全部 `0x0000000000000000`，且 4 例全部 `beforeFirstStore=true`；异常读次数 `count` 为 1.4M–2.8M |
| `fixed` | **0/10 报 UNEXPECTED** |

`count` 高达数百万是关键：`-Xcomp` 下 writer 线程的 `run()` 必须先编译完才能执行第一条指令，
这个窗口有毫秒级，reader 在这期间**反复读到默认 0 上百万次**——这正是“仅在 C2 配置复现”的
真正原因，与 64 位 load 的代码生成无关。

### 旧版本（`legacy`）复现历史失败

| 条件 | 复现率 | 观察值 |
|---|---|---|
| `-Xcomp`，机器空闲 | 0/20 | — |
| `-Xcomp` + `taskset -c 0`（单核争用） | 3/10（JDK-27）、3/6（JDK-27 另一次）、0/8（JDK-8） | **全部 `0x0000000000000000`** |
| `-Xcomp` + `taskset -c 0`，每 JDK 40 轮（最终定量） | **JDK-27 11/40**、JDK-8 0/40 | **全部 `0x0000000000000000`**，其中多条 `beforeFirstStore=true` |
| 默认分层 / `-Xint` + 单核争用 | 0/10 | — |

其中带 `beforeFirstStore=true` 的记录说明观察值出现在 writer 首次 store **之前**——那时字段
只可能是默认 0，撕裂在物理上不可能。`no-seed` 在同一争用条件下同时出现 `PRE_STORE>0` 且
`TORN=0`，与上述结论一致。

### 修复后 jtreg 回归（xuzhu aarch64，新常数 + 种子，每格 3 次）

| 配置 | JDK-8 | JDK-27 |
|---|---|---|
| `-Xcomp` | 3/3 PASS | 3/3 PASS |
| `-Xcomp -XX:TieredStopAtLevel=4` | 3/3 PASS | 3/3 PASS |
| `-Xcomp` + `taskset -c 0` | 3/3 PASS | 3/3 PASS |
| `-Xcomp -XX:TieredStopAtLevel=4` + `taskset -c 0` | 3/3 PASS | 3/3 PASS |

共 24 次 jtreg 运行全部通过；x86（hanqinhu）上 `-Xcomp` 单跑亦通过。

> 跑这些命令时注意：**jtreg 的所有选项必须排在测试文件之前**。写成
> `jtreg ... test.java -javaoption:-XX:...` 会退出码 5 并报
> `Error: Cannot find file: -javaoption:-XX:...`（该参数被当成测试路径），
> 这**不是用例失败**，别误判。

---

## 历史结论（2026-11-12 取证）

| 项 | 证据 |
|---|---|
| 报失败的 sweep 用的是哪版代码 | xuzhu 第一轮 sweep（11-11 23:11 / 11-12 01:54）跑的是远端树 `80d2f2639`（**2026-11-04**），**早于**种子修复 `8af14bc84`（**2026-11-11 12:47**） |
| 实际失败文本 | JDK-8：`java.lang.AssertionError: volatile long/double reads were atomic`（对应旧版第 64 行 `check(!failed, ...)`），确认是无种子版本 |
| 带种子的版本是否复现 | xuzhu v2 sweep（11-12）：JDK-8 十个配置、JDK-27 八个配置，`xcomp` / `xcomp_tiered4` 全部 PASSED |
| 另一处真实缺陷 | 旧常量在 double 侧 `DOUBLE_A_BITS`/`DOUBLE_B_BITS` 低 32 位同为 `0xFFFFFFFF`，任何混合都坍缩回 A/B，double 撕裂**永远检不出**（已在 `a313bc962` 修复） |
| 上游已知 issue | 检索到的 aarch64 C2 volatile 相关 issue（JDK-8394913 / JDK-8397181）均为“**未被使用**的 volatile/acquire 读丢失内存序”，不涉及 64 位读撕裂；本用例的读值被实际使用，不适用 |

结论：历史失败可由 `legacy` 模式在单核争用下复现，观察值恒为 `0x0`（默认值），属**测试误报**；
`strict` 模式在约 180 亿次读中未观察到撕裂。
