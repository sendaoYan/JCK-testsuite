# JDK-27 API 方法级覆盖率分析报告

> 生成日期：2026-11-04。本报告回答「哪些 API 方法没有被测试覆盖」，并说明为什么没有用 jcov 而改用静态 call-site 提取。

## 1. 为什么没有用 jcov（动态覆盖率）

jcov 是 OpenJDK 自带的动态覆盖率工具，本项目的首选方案。但在 **JDK-27** 上实测不可用：

| 尝试 | 结果 |
|---|---|
| jcov 3.0_15 自带 ASM | 抛 `IllegalArgumentException: Unsupported class file major version 71`（其 ASM 太旧，只支持到 Java 26 之前） |
| 打 ASM 9.11.1 补丁后 | instrument 主类时抛 `LinkageError: loader 'app' attempted duplicate class definition`；instrument 引导类（如 `java.lang.StringBuilder`）时 premain 直接崩溃（retransform 引导类失败） |

结论：jcov 3.0_15 的动态 agent 与 JDK-27 存在底层不兼容（类加载/retransform 机制），不是配置或用法问题。备选的 **JaCoCo 0.8.16**（官方已支持 Java 27、ASM 9.11.1）实测：能正确测**应用类**（Hello → 6/7 probes），但 `inclbootstrapclasses=true` 在 JDK-27 上**记不到引导类**——与 jcov 同属「引导类插桩」死结。

## 2. 改用静态 call-site 覆盖分析

既然无法在运行期对 JDK 引导类插桩，改从**测试侧**入手：解析测试自身的编译产物，看测试到底直接调用了/反射引用了哪些 API 方法。

- 方法全集：`CovTools.java methods` 模式用 ASM 9.11.1 遍历 `jrt:` 镜像，dump 出全部非 private（public/protected）方法 → `162495` 条（`jdk27.methods.txt`，临时）。
- 调用点：`CovTools.java calls` 模式对**编译后的测试 .class** 提取每个 `invoke*`（owner+name+descriptor）与字符串字面量 → `1,178,757` 条调用点，覆盖 `15548/15553` 个测试类（5 个 `sun.security.*` 内部包因模块未导出无法编译，忽略）。
- 对比：`tools/analyze_api_coverage.py` 把「方法全集」与「被直接 invoke / 被测试源码反射字符串引用」求差，得到未覆盖方法。

**定义**：一个方法「被覆盖」= 任一测试字节码里存在对该 owner 的同名同签名的 `invoke*`，或该方法名以字符串字面量/`getMethod("…")`/`.name(` 形式出现在该类自己的 Test.java 里。「未覆盖」= 两者都没有。

## 3. 结果（导出 API，即 universe − internal）

> **2026-11-04 tranche 7 后更新**（832 类行为化补测已 push：swing.plaf 403 / sound+imageio 132 / beans+accessibility+script 103 / security.auth+sasl+cert+sql.rowset 91 / rmi+tools+smartcardio+jgss 103）。下方为更新后数字。

| 指标 | 值 | 变化 |
|---|---|---|
| 导出 API 类（有非 private 方法） | 4003 | — |
| 其中行为化（testBlackBox）类 | 3114 | 2949→3114 |
| 仅结构（反射）类 | 889 | 1054→889 |
| 非 private 方法总数 | 42514 | — |
| 已覆盖方法 | 27849（**65.51%**） | +1.64pp |
| 未覆盖方法 | 14665（**34.49%**） | −696 |
| 0 个方法被覆盖的类 | 435 | 486→435 |

**行为化类单独看**（这些是真正做了黑盒行为的）：

| 指标 | 值 |
|---|---|
| 行为化类方法总数 | 32814 |
| 已覆盖 | 24010（**73.17%**） |
| 未覆盖 | 8804（**26.83%**） |

## 4. 解读与边界

1. **「未覆盖」≈ 待行为化的清单**。435 个「0 覆盖」类和 889 个「仅结构」类，基本都是早期只做了 `Class.forName` + `getDeclaredMethods()` 反射的结构测试——它们确实还没做黑盒行为断言。这份 `uncovered_methods.txt` 就是后续行为化补测的权威 TODO。剩余大头（导出 API）：java.awt(168 结构+152 零覆盖)、javax.swing(86+80)、java.util(61)、jdk.jfr/dynalink/jshell/incubator(~119)、java.lang(35)、java.text(23)、java.net/nio(28)、javax.management(16)；`com.sun.*`/`sun.*` 内部包（~330）按用户指示保持结构覆盖不投入行为化。
2. **行为化类里的 26.83% 未覆盖**是更精细的缺口：这些类已经做了黑盒，但仍有一部分方法没被直接调用或命名（例如某工具类只测了 3 个方法里最常见的 1 个）。
3. **静态分析的固有边界**（务必知晓）：
   - 「被引用」≠「被执行」≠「被断言」。直接 invoke 只证明调用点存在，不证明该调用在运行期一定走到、也不证明断言强度。
   - 虚调用/继承/接口转发会漏报或误报（例如调 `List.add` 实际落到 `ArrayList.add`，若测试只写接口类型，owner 记为接口）。
   - 反射动态拼接（`getMethod(nameFromVariable)`）抓不到具体方法名。
   - 因此这是**上界（乐观）估计**：真实的动态覆盖率只会更低。
4. 由于无法对 JDK 引导类插桩，**无法给出「行/分支级动态覆盖」**；本报告是「方法级静态引用覆盖」，作为其替代。

## 5. 复现

```bash
# 1) 编译全部测试（默认包同名冲突 → 逐文件编译到独立目录；预览 API 需 --enable-preview）
# 2) 方法全集 + 调用点（需 /tmp/jcov-tool 下的 ASM 9.11.1 + CovTools.java）
java -cp ".:asm-9.11.1.jar" CovTools methods /tmp/jcov-tool/jdk27.methods.txt
java -cp ".:asm-9.11.1.jar" CovTools calls   /tmp/jcov-tool/calls.txt /tmp/testclasses
# 3) 对比
python3 -I tools/analyze_api_coverage.py
# 产物：tools/out/jdk27.coverage.summary.txt / .uncovered_methods.txt /
#       .behavioral_uncovered_methods.txt / .uncovered_classes.txt / .coverage.per_class.tsv
```

## 6. 后续建议（详见 docs/coverage-enhancement-means.md）

- P0：本方法级 call-site 提取 + `jdeps -verbose:class` 出类级引用，作为行为化补测的权威 TODO。
- P1：JDK-8 vs JDK-27 **差分测试**（复用现有行为化用例）+ japicmp/revapi 兼容扫描。
- P2：jqwik 属性测试局部试点（java.math/集合/java.time，固定种子）；解析器差分 fuzz。
- P3：变异测试 PIT 小试点（无法变异 JDK 引导类，价值有限）。
