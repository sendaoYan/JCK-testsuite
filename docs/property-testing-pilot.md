# jqwik 属性测试试点（property-based testing pilot）

> 目标：在「测试覆盖增强手段」中为**纯函数 / 集合 / 数值 / 时间**类增加一层
> **性质断言**（不变量），作为行为化用例的补充。全部使用 **jqwik 1.10.2 程序化 API**，
> **固定种子**，保证幂等、确定性（不依赖墙钟、不依赖 JUnit Platform 跑器、headless）。
>
> 位置：`tools/property-tests/`（独立 TEST.ROOT，不碰 `JDK-27/API/*` 现有测试，
> 也避开 tranche-9 的 75 个 0 覆盖类）。
>
> 核实日期：2026-11-04。jqwik 版本来自 Maven Central `net/jqwik/jqwik/1.10.2/`。

---

## 1. 结论速览

| 项 | 结论 |
|---|---|
| jqwik 程序化 API 是否可行 | **可行**。plain `main` 里用 `Arbitraries.*` + `Arbitrary.generator(genSize)` + `RandomGenerator.next(java.util.Random)` + `Shrinkable.value()` 即可自跑 N 组输入，**无需 JUnit Platform 跑器**（未走 JUnit Platform Launcher 兜底）。 |
| 运行期最小依赖 | 只需 `jqwik-api-1.10.2.jar` + `jqwik-engine-1.10.2.jar` 两个 jar（Maven 拉取 `net.jqwik:jqwik:1.10.2` 后仅这两个必需；`junit-platform-*`/`opentest4j`/`apiguardian`/`jqwik-time`/`jqwik-web` 程序化路径不需要）。 |
| 固定种子方式 | 每个性质块内 `new java.util.Random(20261104L)` + 固定 `generator(1000)`（genSize），jqwik 生成器相对传入 `Random` 无状态 → 同一 seed+genSize 恒得同一输入序列。 |
| jtreg 集成 | `@library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar` + `@run main <Class>`；独立 TEST.ROOT 下跑 `jtreg tools/property-tests`。 |
| jtreg 结果 | **JDK-27 全绿：`Test results: passed: 5`**。 |

---

## 2. 覆盖的类与性质（7 类 / 18 条）

每条性质在固定种子下自跑 **300 组输入**（`SAMPLES = 300`），任一失败抛 `AssertionError`
并带样本序号；全部通过后打印 `PASS (N properties)`。

| 测试文件 | 覆盖类 | 性质条数 | 性质（不变量） |
|---|---|---|---|
| `BigIntegerPropertyTest.java` | `java.math.BigInteger` | 4 | `add` 交换律；`add` 结合律；`multiply` 交换律；`multiply` 结合律 |
| `BigDecimalPropertyTest.java` | `java.math.BigDecimal` | 2 | `add` 交换律；`multiply` 交换律（`compareTo()==0` 数值相等，不用 `equals()` 防标度漂移） |
| `StringPropertyTest.java` | `java.lang.String` | 3 | `charAt(i)==substring(i,i+1).charAt(0)`；`s==substring(0,i)+substring(i)`；`substring(i,j).length()==j-i` |
| `CollectionsPropertyTest.java` | `java.util.Collections`、`java.util.List`（`ArrayList`） | 3 | `sort` 幂等（已排序再排序不变）；`sort` 后非降序；`List.add` 使 size 逐次 +1 且终值等于 add 次数 |
| `TimePropertyTest.java` | `java.time.Duration`、`java.time.Period` | 6 | `Duration.ofSeconds→toSeconds`、`ofMillis→toMillis`、`ofNanos→toNanos` 往返；`Period.ofYears→getYears`、`ofMonths→getMonths`、`ofDays→getDays` 往返 |

**性质合计：18 条**（4+2+3+3+6）；**类数：7**。

---

## 3. 固定种子与确定性说明

- 常量 `SEED = 20261104L`（今日日期），`generator(1000)` 的 genSize 固定，`SAMPLES = 300`。
- 每个性质块单独 `new java.util.Random(SEED)`：各性质彼此独立、与执行顺序无关，但仍完全确定。
- jqwik 的 `RandomGenerator.next(Random)` 是纯函数式（随机性全部来自传入 `java.util.Random`），
  因此重跑输出逐字节一致（已实测两次运行输出 `PASS` 与中间断言结果完全一致）。
- 不依赖墙钟、默认 Locale/TimeZone/Charset；无 `@key`；headless 安全。

---

## 4. 发现的问题 / 关键 gotcha

1. **jqwik 复合生成器需要 lifecycle 线程**：`Arbitraries.strings()` 与 `Arbitrary.list()/.set()` 等
   “多值”生成器内部经 `Store`/`Memoize` 拿当前测试描述符，在 plain `main` 里直接调用会抛
   `OutsideJqwikException: The current action must be run on a jqwik thread`。
   **规避**：只用标量生成器（`integers`/`longs`/`bigIntegers`/`bigDecimals`/`doubles`/`chars`），
   字符串、列表改由标量生成器手动拼装（见 `StringPropertyTest.randomString`、
   `CollectionsPropertyTest.randomList`）。这是本试点走程序化 API 而非 JUnit Platform 的关键前提。

2. **jtreg `@library` 必须指向 jar 文件本身**：写 `@library /lib`（目录）时，jtreg 把目录归类为
   “packages”（源路径），目录里的 jar 被忽略 → 编译报 `package net.jqwik.api does not exist`。
   正确写法是逐个列出 jar：`@library /lib/jqwik-api-1.10.2.jar /lib/jqwik-engine-1.10.2.jar`，
   jtreg 才会按 “precompiled jar file” 加入 classpath。

3. **`BigDecimal` 用 `compareTo` 不断言 `equals`**：`equals` 对标度敏感（`1.0` vs `1.00` 不等）。
   交换律是数值性质，用 `compareTo()==0` 更稳（虽然 `add/multiply` 的结果标度本就对称）。

4. **程序化路径依赖极简**：实测只放 `jqwik-api` + `jqwik-engine` 两个 jar 即可编译运行；
   不需要 `junit-platform-commons/engine`、`opentest4j`、`apiguardian`、`jqwik-time/web`。

5. **确定性边界**：genSize 不同会得到不同（但仍确定）的输入序列；文档化固定 genSize=1000，
   不要随意改，否则破坏“同一输入必同输出”的对照基线。

---

## 5. 如何集成进 jtreg 主流程（结论）

- **当前形态（推荐保持）**：作为**独立小套件** `tools/property-tests/`（自带 `TEST.ROOT`），
  与 `JDK-27/API/*` 行为化套件**分离**，跑法：
  ```
  jtreg -jdk:$JAVA_HOME tools/property-tests
  ```
  已用 workDir `/tmp/jck27bh-means-property-w` 校验 JDK-27 全绿（`Test results: passed: 5`）。
- **并入主套件的备选**：若要与 JDK-27 主套件同跑，可把 jar 放到 `JDK-27/TEST.ROOT` 的
  `external.lib.roots = ../` 指向的库根，再在各测试 `@library /<jar>` 引用；但会把第三方 jar
  引入主套件路径，试点阶段**不建议**，保持隔离更干净。
- **适用范围**：属性测试只对**有清晰代数/往返不变量**的 API 子集有高 ROI
  （`java.math`、`java.util` 集合、`java.lang.String`、`java.time`、解析/格式化类的
  往返性质）。有状态/IO/并发类难表达，仍是手写行为化的主场。结论：**作为补充层，按需扩点，
  不进全量默认路径**；模板即本目录 5 个文件的写法（标量生成器 + 固定 seed + 手动拼装复合结构）。
- **CI/重复性**：全 headless、无 `@key`、固定种子幂等，可直接进任意 jtreg 批跑。
