# 测试覆盖增强手段调研（除 jcov / JaCoCo 之外）

> 背景：本项目为 OpenJDK（JDK-27 与 JDK-8）的全部非 private API（JDK-27 15553 类、JDK-8 12262 类）编写 jtreg 格式黑盒行为测试。当前结构覆盖（类型级 `enumerate_api.py` 的 gap=0）已 100%，行为化测试在推进中。动态覆盖率工具受阻：jcov 3.0 的 agent 在 JDK-27 上不可用（自带 ASM 太旧，报 "Unsupported class file major version 71"；打 ASM 9.11.1 补丁后仍报 duplicate class definition 或 retransform 引导类崩溃）；JaCoCo 0.8.16 能测应用类、但无法 instrument JDK-27 引导类（`inclbootstrapclasses` 无效）。
>
> 本调研核对方式：GitHub API（`api.github.com/repos/...`、`/releases/latest`）与 Maven Central（`repo1.maven.org/maven2/.../maven-metadata.xml`）。版本号均来自上述实测，拿不准的标注「待核」。
>
> 核实日期：2026-11-04。

---

## 0. 结论速览（投入产出比排序）

| 优先级 | 手段 | 投入 | 产出 | 关键结论 |
|---|---|---|---|---|
| **P0** | 方法级 call-site 静态提取（ASM + jdeps） | 低（已有 `enumerate_api.py` 基础） | 高 | 零运行期插桩，绕开 JDK-27 引导类问题，直接把「类型覆盖」升级到「方法覆盖」 |
| **P1** | 差分测试 JDK-8 vs JDK-27 | 极低（已有双 JDK） | 高 | 天然适配，直接暴露行为分歧 |
| **P1** | 契约/等价类/边界值 方法论 + japicmp/revapi 兼容扫描 | 低 | 高 | 后者是差分的「输入」，告诉你哪里值得差 |
| **P2** | 属性测试 jqwik（纯函数/集合/字符串子集） | 中 | 中 | 只对「有清晰不变量」的 API 子集有效，需固定种子保确定性 |
| **P2** | 差分模糊（JQF/Randoop 对解析器/格式化器） | 中高 | 中 | 局部、谨慎使用；引导类插桩同样受限 |
| **P3** | 变异测试（PIT） | 低（试试） | 低 | 无法变异 JDK 引导类，仅能自检测试辅助逻辑，价值有限 |
| **P3** | 动态覆盖率（JaCoCo/Clover 等） | 已投入 | 低 | 应用类可测、引导类无解；继续追踪上游即可 |

---

## 1. 动态代码覆盖率工具（JaCoCo / Emma / Cobertura / OpenClover）

### 1.1 原理
在类加载或字节码写入阶段对 class 做**插桩**（inject probe 计数器），运行时记录每个方法的进入/分支/行是否被执行，最后聚合成行/分支/方法覆盖率报告。三类插桩：agent 的 `premain` 加载期插桩、`retransform` 重定义、以及离线（offline）字节码改写。

### 1.2 各工具现状（版本均核实）

| 工具 | 最新版本 | 状态 | JDK-27 支持 |
|---|---|---|---|
| **JaCoCo** | **0.8.16**（2026-10-26，Maven + GitHub） | 活跃（2026-11-03 仍提交） | **官方支持 Java 27**（issue #2287），依赖 ASM 9.11.1；引导类需 `inclbootstrapclasses=true` |
| **Emma** | 2.x（~2010 年代） | 停更多年 | 不支持 JDK 8+，排除 |
| **Cobertura** | 2.1.1（2015） | 停更 | 不支持新版 JDK，排除 |
| **OpenClover** | **5.1.1**（Maven） | 仓库活跃（2026-10-23） | 仪器化对最新 JDK 的支持历来滞后，JDK-27 支持**待核**（Atlassian 开源版） |

**关键事实（对本项目）**：JaCoCo 0.8.16 官方已支持 Java 27 类文件（major 71），但其插桩范围只覆盖**应用类加载器**加载的类；对引导/平台类（`java.*`/`jdk.*` 等，由 bootstrap/platform class loader 加载），release notes 明确要求加 `inclbootstrapclasses=true`（JEP 261 之后）。本项目已实测该选项对 JDK-27 引导类**无效**——这是 JaCoCo 的设计边界（JDK 9+ 模块系统下，引导类的插桩需要在 JVM 启动极早期完成，且受 `--patch-module`/CDS/`-Xshare` 等干扰），不是配置错误。

### 1.3 对本场景的价值 / 局限
- **价值**：若能插桩引导类，就能给出「每个 API 方法被行为测试执行到的行/分支覆盖」，是最直接的覆盖增强信号。
- **局限（致命）**：被测对象本身就是 JDK 引导类/平台类，JaCoCo/jcov 都卡在同一处。能测到的只有**测试自身的辅助类**（对覆盖率无意义）。

### 1.4 落地建议
- **不再为此投入**：JaCoCo 对应用类可用、对引导类无解，已与 jcov 同属「引导类插桩」死结；除非 JDK 官方提供稳定的启动期插桩钩子（如 JFR / `-Xlog` 的 hook 化），否则不指望动态覆盖率。
- **保留追踪**：跟踪 JaCoCo 的 `inclbootstrapclasses` 后续改进（issue #2155），以及 jcov 上游是否升级 ASM 并修复 retransform。可归档现有 jcov-patched.jar 与 ASM 9.11.1（`/tmp/jcov-tool/`），不再迭代。

---

## 2. 变异测试（mutation testing）：PIT / Major

### 2.1 原理
对**被测代码**注入人工缺陷（mutation operator，如把 `>` 改成 `>=`、删除方法体、替换常量、交换 `==`/`!=`），然后跑测试套件，看有多少「变异体」被测试杀死（至少一个断言失败）。**突变分数 = 被杀死的变异体 / 总变异体**，用于发现「断言太弱 / 测试根本没走这条分支」的无效用例。

### 2.2 工具现状

| 工具 | 最新版本 | 状态 |
|---|---|---|
| **PIT（pitest）** | **1.30.1**（2026-10-14，`hcoles/pitest`） | 活跃（2026-11-03 提交），1.30.1 已把 ASM 升到 9.11（可解析 Java 27 类文件） |
| **Major** | 无明确活跃发布（Ulm 大学，mutation-testing.org） | 年久失修，JDK 支持旧，**待核** |

### 2.3 对本场景的价值 / 局限
- **PIT 的经典价值**：发现「测试能跑过但断言空转」的假绿。这对本项目的行为化测试确实有理论价值——很多反射式/白盒用例只断言「可调用」而不断言结果。
- **根本限制**：PIT/Major 变异的是**被测代码字节码**。本项目的被测对象是 JDK 引导/平台类，PIT 同样无法变异这些类（与覆盖率相同的引导类插桩问题）。因此：
  - 无法用 PIT 直接衡量「我的 JDK API 测试有没有把断言写实」。
  - 唯一可能的应用：把测试里**抽离出的纯逻辑辅助方法**（如校验器、格式化断言辅助）当作被测类，用 PIT 自检这些辅助逻辑是否写对——但这是「测测试代码」，边际价值低。

### 2.4 落地建议
- **不纳入主流程**。若团队想低成本验证「断言太弱」问题，可选做一次**小型试点**：挑 3–5 个纯逻辑 API（如 `java.math.BigInteger`、`java.util.regex.Pattern` 的测试辅助函数），把等价逻辑复制成应用类后用 PIT 跑一次，感受突变分数；不建议对全量套件做。

---

## 3. 结构化 / 静态覆盖分析：字节码 call-site 提取

### 3.1 本项目已有手段（类型级）
`tools/enumerate_api.py` 做的是**类型级结构覆盖**：
1. 解析 JDK 镜像/rt.jar 里每个 `.class` 的 `access_flags`，枚举所有非 private 类型（15553 / 12262）。
2. 检查 `JDK-{27,8}/API/<包路径>/<Simple>/` 目录是否存在（`<Simple>Test.java`，包路径按 fqcn 的 `.` 展开为 `/`），算出 `gap.txt`（缺失类型）。
3. 对 JDK-9+ 同时映射类型→模块/包，区分「无资格导出」与「内部（需 `@modules ...:+open`）」。

这是**存在性**覆盖：证明「每个类型都有至少一个测试文件」，不证明「测试真的调用了该类型的哪些方法、跑没跑通、断言实不实」。

### 3.2 call-site 提取原理（方法级，升级方向）
对 jtreg 编译出的**测试 `.class` 文件**（而非 JDK 本身）做静态解析，统计测试字节码里直接引用/间接引用了哪些 JDK 方法：
1. 用 ASM `ClassReader` 遍历每个测试类，捕获：
   - `MethodInsnNode`（`invokevirtual/static/special/interface`）→ `(owner, name, descriptor)`。
   - `FieldInsnNode`（`getstatic/putstatic/getfield/putfield`）→ 字段引用。
   - `InvokeDynamicInsnNode` 的 bootstrap 方法/句柄。
   - `LdcInsnNode` 里的字符串常量 → 识别 `Class.forName("...")`、`getMethod("...")`、`getDeclaredMethod("...")` 等**反射字符串引用**。
2. 反向生成 JDK 侧**方法级权威枚举**：用 ASM 扫 jimage/rt.jar 里每个非 private 类型的所有 public/protected 方法（`name+descriptor`），得到「API 方法全集」。
3. 两者做差 → **未覆盖方法列表**（method-level gap）。

### 3.3 边界与局限（重要）
- **「被引用」≠「被执行」≠「被断言」**：字节码里出现一次 `invoke` 只说明静态引用了该方法，不代表该调用点在运行时被走到，更不代表断言有效。它是**可达性/引用**信号，比类型覆盖细一个量级，但仍不是行为保证。
- **虚调用/多态**：`invokeinterface` 的 owner 是接口/抽象类，真正落地的实现类（如 `List.add` 落到 `ArrayList.add`）不会被捕获；需要额外的「调用图/实现类枚举」才能归并到具体实现方法。
- **继承**：调用子类继承来的方法（子类未覆写）时，owner 是子类，父类方法不会被标记；需按继承链向上归并。
- **反射字符串**：`Class.forName`/`Method.invoke` 只能靠字符串常量近似识别，动态拼接的类名/方法名会漏；白盒反射用例会因此被高估/低估。
- **重载消歧**：必须以 `name+descriptor` 而非 `name` 计，否则重载方法会误判。
- **`invokedynamic`/lambda**：lambda 体里的 API 调用封装在合成方法里，需展开合成方法体才不漏。
- **对 JDK 侧的引导类无插桩问题**：因为解析的是**测试类**字节码，完全不碰 JDK 运行期，绕开了覆盖率工具的致命点——这是该方法最大的优势。

### 3.4 可用工具
- **jdeps**（JDK 自带，零依赖）：`jdeps -verbose:class` 可列出每个测试 class 引用了哪些 JDK **类型**（类级 call-site/依赖提取）；`jdeps -recursive` 递归。JDK-27 自带的 jdeps 为 27.0.1，天然解析 71 号类文件。**注意**：jdeps 给的是**类级**依赖，不是方法级；但对「哪些类型真的被引用」已经能直接产出 `tools/out/*.gap` 的补充。
- **ASM 9.11.1**（`org.ow2.asm:asm`，Maven 最新 9.11.1）：方法级提取的自研脚本基础，本项目已备于 `/tmp/jcov-tool/asm-new/`。
- 现成的 call-site/依赖工具可参考：`jdeps`、`DependencyFinder`（年久）、`Revapi` 的 API 元素级模型（见第 8 节）。

### 3.5 落地建议（**P0，最优先**）
1. 在 `tools/` 下新增 `enumerate_callsites.py`（用 ASM 9.11.1 或纯 struct 解析，与 `enumerate_api.py` 风格一致），对 jtreg 产出目录（如 `JTwork/`）里编译出的 `**/*.class` 做 call-site 提取。
2. 同时生成「JDK 方法级全集」`tools/out/<jdk>.methods.txt`（owner\`name\`descriptor）。
3. 产出 `tools/out/<jdk>.method_gap.txt`：未被任何测试静态引用的方法。用它与 `enumerate_api.py` 的 `gap.txt` 并行，把覆盖口径从「类型」升级到「方法」。
4. 对反射字符串引用单列一类，标注「反射引用（近似）」，避免与直接 invoke 混淆。
5. 把虚调用/继承归并做成**可选**增强，先出「直接引用 + 反射字符串」的最小可用版即可，边界在报告里写清。

---

## 4. 基于规范 / 契约的测试生成：模型驱动 / 等价类 / 边界值 / 组合（pairwise）

### 4.1 原理
不靠运行期插桩，而是**从规范推导用例**，系统化地保证输入空间覆盖：
- **等价类划分**：把输入域按「导致相同行为」分成若干等价类，每类取代表值。
- **边界值分析**：对每个等价类取边界点（最小值、最大值、`±1`、空串/空集合/null）。
- **组合测试（pairwise）**：参数组合爆炸时用 pairwise（两两组合）压缩用例数。
- **模型驱动**：用状态机/前置-后置条件（如 JML、Alloy、有限状态机）生成/校验用例。

### 4.2 工具
这类多为**方法论**，工具是辅助枚举：
- **组合测试**：`PICT`（微软，pairwise 命令行）、`Jenny`、`ACTS`（NIST）、JUnit 平台的 pairwise 插件（如 `junit-pioneer` 的 `@CartesianTest`，但 pairwise 需自配）。
- **模型驱动/契约**：`JML` 系（OpenJML）、`Spock`/`Kotest` 的表驱动（table-driven）、`Alloy`（模型反例）。
- 对本项目而言，等价类/边界值是**写行为化 DESIGN.md 时应内置的清单**，无需引入重型工具。

### 4.3 对本场景的价值 / 局限
- **价值高且零插桩**：JDK API（`java.lang`/`util`/`time`/`math`/`text`/`regex` 等）大多有明确文档契约，等价类/边界值天然适配——例如 `Integer.parseInt`（合法/非法/边界/基数/前导符号/空串）、`BigDecimal` 舍入模式×标度×值域、`SimpleDateFormat` 的格式串边界。
- **局限**：它提升的是**输入/输出覆盖**，不直接回答「哪些方法还没被调到」；与第 3 节的 call-site 提取互补（一个补「什么没测」，一个补「怎么把测的做全」）。

### 4.4 落地建议
- 把「等价类 + 边界值 + pairwise 提示」固化进 `BEHAVIORAL_TESTING.md` 的用例要求，作为每个 `DESIGN.md` 第 4 部分的**必填清单**：每个被测方法列出等价类、边界值、异常类，并映射到具体断言。
- 对参数组合多的工厂方法（如 `BigDecimal` 构造、`Pattern` 标志位、格式化器构造），用 PICT 或自研 pairwise 脚本生成组合表，再手写断言（保持确定性）。

---

## 5. Fuzzing / 随机化测试：JQF、Randoop、EvoSuite

### 5.1 原理
- **Randoop**：反馈式随机生成方法调用序列，比对观察到的行为与「违反通用契约（如 equals/hashCode 一致性、序列化往返）」的 oracle 来自动找 bug。生成的是 JUnit 回归测试。
- **EvoSuite**：进化算法（遗传算法）最大化分支覆盖率，生成 JUnit 套件。
- **JQF + Zest**：覆盖率引导的结构化 fuzzing——用 ASM 插桩拿分支覆盖作为适应度，变异结构化输入（如字节流、文本、对象），对解析器/反序列化器最有效。

### 5.2 工具现状

| 工具 | 最新版本 | 状态 | JDK-27 适配 |
|---|---|---|---|
| **Randoop** | **4.3.4**（2025-06-06） | 活跃 | 新 JDK 类文件支持待核（其内部用 ASM） |
| **EvoSuite** | **1.2.0**（2021-09-16） | 基本停更（最后 push 2025-02） | 只支持到较旧 JDK，**待核** |
| **JQF** | **jqf-3.0**（2026-07-22） | 活跃 | 引导类/最新 JDK 插桩能力**待核** |

### 5.3 对本场景的价值 / 局限
- **根本限制**：Randoop/EvoSuite 的输入是**被测应用类字节码**，目标是「为应用类生成测试」。本项目被测对象是 JDK 引导类，让它们直接对 JDK 生成测试既不现实（插桩同样卡引导类）也不必要（JDK 已自带大量测试）。
- **局部可用点**：**差分模糊**（differential fuzzing）——用同一输入喂 JDK-8 与 JDK-27 的解析器/格式化器，比较输出差异，能发现规范允许之外的行为漂移。典型目标：`java.util.regex`、`java.text`（NumberFormat/DateFormat/MessageFormat）、`java.net.URI/URL`、`java.util.Properties`、XML/JSON 解析、`java.lang.Integer/Long/Double` 的 parse。这类 API 输入是「字符串/字节流」，JQF 的结构化 fuzzing 最合适。
- **局限**：JQF 自身的插桩也依赖 ASM agent，对引导类的分支覆盖引导可能失效（**待核**）；即便不引导，也可用纯随机字符串 + 双 JDK 跑同一套解析逻辑做**黑盒差分**（不插桩）。

### 5.4 落地建议
- **不做全量**，仅在「有明确文本/二进制输入、JDK-8 与 27 行为可能漂移」的解析类上做**黑盒差分 fuzz**：固定种子，双 JDK 跑同一批输入，断言输出一致（或差异在规范允许内）。
- 优先自研轻量 harness（不依赖 JQF 的引导类插桩），JQF 可作为可选后端；EvoSuite 基本不引入。

---

## 6. 属性测试：jqwik、QuickTheories

### 6.1 原理
不写具体输入，而是声明**不变量/性质**（property），框架随机生成大量输入去检验。典型性质：`List` 的 `add→size` 单调性、`sort` 后有序、`equals/hashCode` 一致性、往返性质（`serialize→deserialize`、`encode→decode`、`toString→parse`）、交换律/结合律（`BigInteger.add` 交换律）。

### 6.2 工具现状

| 工具 | 最新版本 | 状态 | 说明 |
|---|---|---|---|
| **jqwik** | **1.10.2**（2026-10-10，Maven + GitHub） | 活跃 | JUnit Platform 原生属性测试，功能最全 |
| **QuickTheories** | **0.26**（Maven；GitHub 0.25，2018，最后 push 2020） | 停滞 | Java 8 属性测试，简单 |

### 6.3 对本场景的价值 / 局限
- **价值**：对**纯函数 / 集合 / 数值 / 字符串 / 时间**类，属性测试能用很少代码覆盖很大输入域，天然补「边界值穷举」的不足。JDK 里大量 API 有清晰代数性质（如 `BigInteger`/`BigDecimal` 的交换律、`String` 的 `charAt`/`substring` 关系、`Collections` 排序幂等）。
- **局限**：
  - **随机性 vs 本项目「确定性」硬约束**：jqwik 默认随机种子会导致用例不幂等，必须**固定种子**（`@Seed` 或 `--property-seed`），否则违反健壮性要求。
  - 属性测试偏向「验证性质」，不直接产出「哪个方法没覆盖」。
  - 有状态对象的属性（如并发容器、IO 资源）难表达，不是主力。

### 6.4 落地建议
- **局部试点**：在 `java.math`、`java.util` 集合、`java.time` 的少量类上，用 jqwik 固定种子写属性断言（往返、对称性、不变式），作为行为化用例的**补充层**，与手写组合断言并存。
- 不改写现有 jtreg 结构：属性用例可作为 jtreg `@run main` 里的一段 `@Test`/自调用逻辑（或单独 Maven/Gradle 侧跑），保持 jtreg 主流程不受影响。

---

## 7. 差分测试（differential testing）：JDK-8 vs JDK-27

### 7.1 原理
同一个用例（相同输入、相同操作序列）在**两个被测实现**上各跑一遍，比较可观察输出。若两实现都应遵循同一/相近规范，则输出差异即「候选缺陷或规范演化点」。这是本项目**天然具备**的条件：JDK-8 与 JDK-27 双跑同一套 jtreg 行为化用例。

### 7.2 对本场景的价值 / 局限
- **价值**：
  - 直接暴露「JDK-8 与 27 行为漂移」，而行为漂移正是 conformance 测试最该抓的东西。
  - 复用现有测试资产：行为化用例本就双 JDK 存在（`JDK-8/API/...` 与 `JDK-27/API/...`），把同一用例的**断言输出/异常类型**抽出来 diff 即可。
- **局限**：
  - 两版本**规范本身在演进**（新增默认方法、新 API、改签名），差异不一定是 bug，需要人工归类为「规范允许 / 有意变更 / 回归」。
  - 输出 diff 需要**可序列化、可排序、去平台噪声**（时间戳、随机 ID、哈希顺序、线程名都要归一化），否则假阳性淹没真信号。

### 7.3 落地建议
- 建一个**轻量 diff harness**：对每个行为化用例，在两 JDK 上跑，采集「方法返回值 / 抛出的异常类型」清单，归一化后 diff。
- 优先 diff **导出 API 的稳定契约**（`java.lang`/`util`/`math`/`text`/`time`/`regex`），内部包（`sun.*`/`com.sun.*`/`jdk.internal.*`）不 diff（本就非稳定）。
- 与第 8 节的 japicmp/revapi 联动：**先看二进制/源码兼容差异，再决定哪些类值得做行为差分**。

---

## 8. 其他：文档/API 扫描 + 测试优先级/缺陷预测

### 8.1 二进制/源码兼容扫描

| 工具 | 最新版本 | 说明 |
|---|---|---|
| **jdeps** | JDK 自带（本项目 27.0.1） | `jdeps --jdk-internals`（JDK-8 侧查内部 API 使用）、`jdeps -verbose:class`（类级依赖） |
| **japicmp** | **0.26.5**（2026-10-16） | 两个 jar/镜像做**二进制 + 源码兼容性**对比，输出新增/删除/变更的类与方法 |
| **revapi** | **0.15.1**（Maven） | API 变更追踪，插件化，能按语义等价性过滤（如新增默认方法是否 breaking） |

- **价值**：japicmp/revapi 直接对比 **JDK-8 vs JDK-27 的 API 表面**，产出「新增/删除/签名变更方法清单」——这是差分测试（第 7 节）和补齐覆盖的**权威输入**：哪些方法在 27 里没了、哪些是新加的、哪些签名变了，测试应重点覆盖这些差异点。
- **局限**：只做**表面（签名）**比对，不做**行为**比对；二者互补。

### 8.2 测试优先级 / 缺陷预测（学术，简要）
- **JNose**：基于机器学习的 **test smell（测试坏味道）检测器**（Python，原版为学术工具）。可扫出断言冗余、Eager Test、Mystery Guest 等坏味道，帮助定位「弱断言用例」。有非官方 CLI 前端 `ifsazevedo/jnose-cli-qsoft`（2026-10 活跃）。**仅作参考**，不纳入主流程。
- **TestSpark**：JetBrains 学术插件，结合 LLM + 搜索式技术**生成单元测试**。官方仓库未能通过 GitHub 搜索稳定定位（**待核**），属研究原型。
- **落地**：这两类工具对本项目（JDK conformance、量级 30 人年）ROI 低，仅 JNose 可作为「弱断言用例」的**离线审计**跑一次，输出坏味道清单供人工复审，不做持续集成。

---

## 9. 本项目推荐优先级（按投入产出比）

1. **【P0，立即做】方法级 call-site 静态提取**（第 3 节）
   - 在 `tools/enumerate_api.py` 基础上新增 `enumerate_callsites.py`（ASM 9.11.1 或纯 struct 解析），产出 `tools/out/<jdk>.method_gap.txt`。
   - 配合 `jdeps -verbose:class` 先出类级引用（零成本），再升级方法级。
   - 零运行期插桩、绕开引导类死结，把覆盖口径从「类型存在」升级到「方法被引用」。

2. **【P1】差分测试 JDK-8 vs JDK-27**（第 7 节）
   - 复用双 JDK 资产，建归一化 diff harness，先做导出 API 稳定契约。

3. **【P1】兼容性扫描 japicmp/revapi + 等价类/边界值方法论固化**（第 8.1、4 节）
   - japicmp 对比 JDK-8 vs 27 API 表面 → 差异点清单 → 喂给差分测试与补测。
   - 把等价类/边界值/pairwise 清单写进 `BEHAVIORAL_TESTING.md` 作为 `DESIGN.md` 必填项。

4. **【P2】属性测试 jqwik 局部试点**（第 6 节）
   - 对 `java.math`/`java.util` 集合/`java.time` 的纯函数类，固定种子写往返/对称/不变式断言。

5. **【P2】差分模糊（解析器/格式化器）**（第 5 节）
   - 仅对文本/二进制输入类（regex/text/net/math 解析）做黑盒差分 fuzz，自研轻 harness，不依赖引导类插桩。

6. **【P3】变异测试 PIT 小试点**（第 2 节）
   - 只用于自检测试辅助逻辑，不进主流程。

7. **【P3】动态覆盖率**（第 1 节）
   - JaCoCo/jcov 引导类插桩已确认无解；保留上游追踪，不再投入。
