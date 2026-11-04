# PIT 1.30.1 变异测试试点报告

> 结论先行：PIT 1.30.1 **可以在 JDK-27 上正常运行**（解析/变异 Java 27 类文件，major 71），作为「自检测试辅助逻辑」的小型试点产出 80% mutation score；存活变异体清晰地区分出两类信号——**弱断言/假绿**（可修）与**等价变异体**（PIT 固有噪声，需人工识别），为 `coverage-enhancement-means.md` 第 2 节的「P3 变异测试」结论提供了实测依据。

---

## 1. 目标与范围

按 `docs/coverage-enhancement-means.md` 第 2 节结论，本项目被测对象是 JDK 引导/平台类，PIT 无法变异这些类；唯一可行的是把「测试里抽离出的纯逻辑辅助方法」当作应用侧被测类，用 PIT 自检这些辅助逻辑是否写实。本试点：

- 抽 4 个纯逻辑 API 的「测试辅助等价逻辑」为应用类；
- 配 JUnit 4 测试（断言写实，另含 1 个刻意弱断言位）；
- 用 PIT 1.30.1 跑一次，记录 mutation score 与存活变异体；
- 结论：该方法对本项目的适用边界与落地建议。

## 2. 环境与工具

| 项 | 值 |
|---|---|
| JDK | Temurin 27.0.1.0.2+1（`/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1`） |
| 被测类字节码 | 用 JDK-27 javac 编译，class major version = **71** |
| PIT | `org.pitest:pitest-command-line:1.30.1`（含 ASM 9.11） |
| 测试框架 | JUnit 4.13.2 + hamcrest-core 1.3（PIT 默认 `--testPlugin junit` 直接可用） |
| 产物目录 | `tools/pit-pilot/`（源码 `src/`、测试 `test/`、报告 `pit-out/`、可复现脚本 `run.sh`） |

### 2.1 关键 gotcha：`pitest-command-line` 1.30.1 的 jar **不是**完全 shaded

`pitest-command-line-1.30.1.jar` 只 shade 了 `jopt-simple` 与 `pitest-html-report`，核心类（如 `PluginServices`）在 `pitest` / `pitest-entry` 里。直接 `java -jar pitest-command-line-1.30.1.jar` 会报 `NoClassDefFoundError: org/pitest/mutationtest/config/PluginServices`。

需要的完整运行 classpath（均已下载到 `tools/pit-pilot/lib/`）：

```
pitest-command-line-1.30.1.jar   # 命令行入口 + html-report + jopt-simple（shaded）
pitest-entry-1.30.1.jar          # 入口逻辑（shaded asm + asm-util）
pitest-1.30.1.jar                # 核心
asm-9.11.jar
asm-commons-9.11.jar
commons-text-1.14.0.jar
commons-lang3-3.18.0.jar         # commons-text 的传递依赖
```

入口主类：`org.pitest.mutationtest.commandline.MutationCoverageReport`。

## 3. 被测类清单（应用侧「测试辅助等价逻辑」）

4 个纯逻辑辅助类，均为测试里会复用的校验器/断言辅助：

| 类 | 抽取的等价逻辑 | 突变面 |
|---|---|---|
| `BigIntegerArithmeticVerifier` | BigInteger 加法/乘法交换律、加法结合律、乘法对加法分配律、`mod` 结果落在 `[0,|m|)` 的不变量 | 布尔返回值、条件边界、条件取反 |
| `PatternMatcherVerifier` | 正则 `matchesAll`/`countMatches`/`matchesAny` 校验 | 循环条件、`count++`、短路布尔、null 分支 |
| `DecimalFormatHelper` | 金额拆「符号 + 分」、容差比较 `<=`、定点舍入、百分比格式化 | 算术 `*`/`-`、`<`/`>=` 边界、返回空串/0.0 |
| `SortInvariantVerifier` | 排序不变量：非降有序 + 排序是置换 | 循环边界 `i<size-1`、`compareTo>0`、`Collections.sort` 调用、短路 |

测试 4 个 JUnit 类共 **21 个用例**，全部先绿（`OK (21 tests)`）。其中 `DecimalFormatHelperTest.formatPercentWeakAssertionOnlyChecksNonNull` 是**刻意弱断言位**：只 `assertNotNull(formatPercent(12.5))`，不断言内容。

## 4. PIT 运行命令

见 `tools/pit-pilot/run.sh`，核心参数：

```
--targetClasses 'pitpilot.*'
--targetTests   'pitpilot.*Test'
--excludedClasses 'pitpilot.*Test*'   # 关键：排除测试类自身，否则 PIT 会把 assertTrue 调用当变异对象
--sourceDirs src,test
--classPath 'classes,testclasses,lib/junit-4.13.2.jar,lib/hamcrest-core-1.3.jar'   # 注意是逗号分隔
--outputFormats HTML,XML
```

两个必踩参数坑：`--classPath` 用**逗号**分隔（不是冒号）；`--targetClasses 'pitpilot.*'` 会同时命中 `*Test`，必须加 `--excludedClasses 'pitpilot.*Test*'`，否则会产出 44 个「删除 assert 调用」的无效存活变异体淹没结果。

## 5. 结果：mutation score

```
Line Coverage (for mutated classes only): 54/63 (86%)
Generated 66 mutations, Killed 53 (80%)
Mutations with no coverage 1. Test strength 82%
```

按变异算子拆解（KILLED 含 1 个 TIMED_OUT）：

| 变异算子 | 生成 | 杀死 | 存活 | 未覆盖 | 备注 |
|---|---:|---:|---:|---:|---|
| NegateConditionals | 20 | 18 | 1 | 0 | 另有 1 个 TIMED_OUT（无限循环，按杀死计） |
| BooleanTrueReturnVals | 14 | 9 | 5 | 0 | 存活者全为等价变异体 |
| ConditionalsBoundary | 10 | 7 | 3 | 0 | 1 弱断言 + 1 边界缺失 + 1 等价 |
| BooleanFalseReturnVals | 8 | 8 | 0 | 0 | — |
| Math | 5 | 4 | 1 | 0 | 存活者为弱断言位 `*100→/100` |
| EmptyObjectReturnVals | 4 | 2 | 1 | 1 | 存活+未覆盖均在弱断言位 `formatPercent` |
| PrimitiveReturns | 2 | 2 | 0 | 0 | — |
| VoidMethodCall | 2 | 1 | 1 | 0 | 存活者为「第二个 sort 冗余」 |
| Increments | 1 | 1 | 0 | 0 | — |
| **合计** | **66** | **53 (80%)** | **12** | **1** | — |

## 6. 存活变异体分析（三类，这是试点的核心产出）

### 6.1 弱断言 / 假绿（可修，最值得关注）

1. **`formatPercent`（刻意弱断言位）**——5 个变异体存活/未覆盖：
   - `MathMutator`：`value * 100.0` → `/ 100.0` 存活；
   - `ConditionalsBoundary`：`pct >= 0` → `> 0` 存活；
   - `NegateConditionals`：取反存活；
   - `EmptyObjectReturnVals`：`return "+" + pct` → `""` 存活；
   - `EmptyObjectReturnVals`：`return Long.toString(Math.round(pct))`（负数分支）**NO_COVERAGE**——测试根本没走到负数分支。
   - 根因：`assertNotNull(formatPercent(12.5))` 只断言非 null、且只用正数输入，完全不断言内容、不覆盖负数分支。这正是「测试能跑过但断言空转」的假绿标本。

2. **`roundToScale` 边界缺失**——`ConditionalsBoundary`：`scale < 0` → `<= 0` 存活。
   - 根因：测试只测了 `scale = -1`（抛异常）和 `scale = 1, 2`（正常），**没测 `scale == 0`**。`< 0` 与 `<= 0` 只在 `scale == 0` 处行为不同，边界值缺失导致变异体杀不死。

3. **`isPermutation` 测试数据不充分**——`VoidMethodCall`：第二个 `Collections.sort(sb)` 删除后存活（第一个 sort 被杀死）。
   - 根因：测试输入 `b = [1,2,3]` 本来就有序，第二个 sort 对它是 no-op。若把 `b` 换成未排序的 `[2,3,1]`，两处 sort 的删除都会被杀死。

### 6.2 等价变异体（PIT 固有噪声，非缺陷）

4. **`BigIntegerArithmeticVerifier` 的 4 个 `BooleanTrueReturn` 存活**：`isCommutativeAdd/Multiply/isAssociative/isDistributive` 本身恒真，`替换成 return true` 是等价变异体，无法杀死。
5. **`isValidMod` 的 2 个存活**：`r.compareTo(absM) < 0` → `<= 0`（mod 定义保证严格小于，等价）；`return ...` → `return true`（`m != 0` 分支恒真，等价）。其中 `r.signum() >= 0` → `> 0` 的边界变异体被 `a` 为 `m` 倍数（余数 0）的用例**正确杀死**，说明断言是写实的。

> 经验：恒真校验器（代数恒等式、不变量）天然产生 `return true` 等价变异体，突变分数会被系统性压低；这属于信号而非弱点，报告时需把等价变异体单列，不要误读为「测试没写实」。

### 6.3 无限循环（TIMED_OUT，按杀死计）

6. **`PatternMatcherVerifier.countMatches`**：`while (m.find())` 条件取反后，对无匹配输入（`countMatches("z","abc")`）变成无限循环，PIT 报 TIMED_OUT。这是 PIT 的经典行为：`while` 条件变异可能制造死循环，默认超时按杀死计。

## 7. 关键 gotcha（含「PIT 1.30.1 在 JDK-27 上能否正常运行」）

1. **能正常运行**：PIT 1.30.1（ASM 9.11）在 JDK-27 JVM 上，成功解析并变异了 JDK-27 javac 编译出的 major 71 类文件，全程无 `Unsupported class file major version` 报错。对比 jcov/JaCoCo 卡在「引导类插桩」，PIT 对**应用类**字节码变异没有版本障碍。
2. `pitest-command-line` 1.30.1 的 jar **非完全 shaded**，需手动补齐 `pitest`/`pitest-entry`/`asm`/`asm-commons`/`commons-text`/`commons-lang3`（见 §2.1）。
3. `--classPath` 是**逗号**分隔，误用冒号会被当成单个不存在的路径 → 报 `No mutations found`。
4. `--targetClasses 'pkg.*'` 会误命中同包测试类，必须 `--excludedClasses 'pkg.*Test*'`，否则「删除 assert 调用」变异体会淹没真实结果。
5. `while` 条件变异会制造无限循环，PIT 报 TIMED_OUT 并计入杀死；对「计数型」辅助方法要有预期。
6. 恒真/不变量型辅助逻辑会带来 `return true` 等价变异体，压低分数，需人工归类，不能当作弱断言。

## 8. 对本项目（JDK 引导类为被测对象）的适用结论与落地建议

- **结论维持 `coverage-enhancement-means.md` 第 2 节 P3 定位不变**：PIT 无法变异 JDK 引导类，不能直接衡量「JDK API 测试的断言质量」。本次实测确认它**只能自检抽离出的应用侧辅助逻辑**，边际价值有限，不纳入主流程。
- **有限的真实价值**：当行为测试把「校验器/断言辅助」抽成应用类后，PIT 能快速暴露三类问题——(a) 只断言非 null/可调用而不断言内容的假绿；(b) 等价类/边界值缺失（如 `scale==0`、余数 0）；(c) 测试数据恰巧让某步操作冗余（如 sort 输入已有序）。这三点与第 4 节「等价类/边界值方法论」高度一致，可作为**离线审计**手段，不定期对关键纯逻辑辅助类跑一次。
- **落地建议**：
  1. 不建持续集成门禁；只在「把测试辅助逻辑抽成应用类」的局部模块做一次性审计，产出存活变异体清单交人工复审。
  2. 报告里把**等价变异体**（恒真校验器）与**弱断言/边界缺失**分列，避免噪声误导。
  3. 若要长期复用，把 `tools/pit-pilot/run.sh` 与 `lib/` 的 jar 集合作为可复现基线；新辅助类直接放进 `src/pitpilot/`、测试放 `test/pitpilot/` 即可。
  4. 真正的「断言太弱」主战场仍在方法论层（`DESIGN.md` 强制等价类/边界值/异常类 + 写实断言），PIT 只作为抽查手段。
