# API 行为化（黑盒）测试方法论

## 目标与定位

结构覆盖（`tools/enumerate_api.py` 的 gap=0）只验证「每个非 private 类型能反射加载、形态符合二进制」——是**存在性**验证。
行为化测试验证**黑盒行为是否符合 Java 用户的预期**：站在使用者视角，**创建实例 → 多步 API 组合操作 → 断言可观察行为**。

**白盒（结构/反射）与黑盒（行为）合并到同一个用例文件**：`JDK-{27,8}/API/<包路径>/<Simple>/<Simple>Test.java`（包路径按 fqcn 的 `.` 展开为 `/`，如 `java/util/HashMap/HashMapTest.java`）内分前后两部分——前半白盒（`Class.forName` + 修饰符/继承/成员形态，保留/补齐已有），后半黑盒（组合操作行为，新增）。行为化是**追加**而非替换，也不另开新文件。

- **行为化范围**：导出/文档化 API（`java.*` / `javax.*` / `jdk.*` 导出面）——这是大部分用户会看的。
- **不投入**：内部包（`com.sun.*` / `sun.*` / `jdk.internal.*`）——已 100% 结构覆盖，用户不读，行为化价值低。

## 工作流程：每类一篇测试方案（DESIGN.md）

**每个 API 类产出一篇正式测试方案** `JDK-{27,8}/API/<包路径>/<Simple>/DESIGN.md`，与 `<Simple>Test.java` 同目录。方案必须讲清四部分：

1. **API 提供了哪些功能**：构造器/工厂、核心操作、查询、转换、工具方法等，按方法分组列出。
2. **Java 用户如何使用**：用户怎么构造/工厂实例化，**每个方法**的典型调用方式，以及真实会串起来的组合链路（如 `Map.put → get → containsKey → remove → size`）。
3. **开源代码实际用法调研**：调研 GitHub/开源项目怎么使用这个类及**每个方法**。优先 `WebSearch`（知名项目 Spring/Guava/Apache Commons/Netty/JDK 自身 `test/jdk` 等）＋ `~/git/jdk8u-dev`、`~/git/jdk25u-dev` 源码里的自用；给出 2–5 个真实用法片段并标注来源。web 不可用则用 `test/jdk` + JDK 源码，明确标注来源。**不凭记忆编造用法**。
4. **测试用例设计 + 自动化实现**：从 1–3 推导测试场景（正常路径、组合操作、边界、异常），并指向 `<Simple>Test.java` 黑盒部分的具体断言。

测试本身仍遵循下述用例要求。DESIGN.md 是「为什么这么测」的依据，测试是「怎么测」的落地。

## 用例要求

- **多步组合操作**，不是单方法断言。例：`ArrayList` 要测 `add → addAll → remove → contains → indexOf → iterator → subList → toArray → clear` 之间的状态联动。
- 断言**可观察行为**：返回值、状态（size/内容/顺序）、不变量、异常（稳定时断异常类型 + message）。
- **确定性**：
  - 固定输入；迭代无序集合时先排序再断言（不要断言 HashMap 迭代顺序）。
  - 日期/格式化/排序类测试固定 `Locale` 与 `TimeZone`。
  - 不依赖墙钟（用固定 `Instant`/`Clock`）；不用 `sleep`，并发用 `CountDownLatch`/`BlockingQueue` 等确定性原语。
- **headless-safe**：不碰图形/声卡/打印机/网络（除非被测 API 本身就是这些，且可对 localhost/内存安全测）；纯 headless 不加 `@key`。
- **行为权威**：`javap`（精确签名）+ `~/git/jdk8u-dev`、`~/git/jdk25u-dev` 源码 + JDK 自带 `test/jdk` 用例。不凭记忆；JDK-8 与 JDK-27 行为有差异，分别按各自实际行为写。

## 健壮性（硬性要求）

黑盒用例必须**多次运行一致、多用例并跑一致、跨平台一致**：

1. **多次运行一致（幂等/确定）**：不依赖墙钟（固定 `Instant`/`Clock.fixed`）、无种子随机（固定种子）、无序集合迭代顺序（先排序再断）、`System.gc()` 效果（WeakHashMap/虚引用/finalize 规避或只断「可调用」）、平台默认 Locale/TimeZone/Charset（固定 `Locale.ROOT`/`UTC`/`UTF-8`）。
2. **多用例并跑一致（无全局副作用）**：不改 `System` 属性/`setOut`/`setErr`/`setIn`/`TimeZone.setDefault`/`Locale.setDefault`（改则 finally 恢复）；文件用独立 `createTempDirectory` 并清理；不绑固定端口/不做真实网络；不遗留非 daemon 线程/未关 Executor；不用 `System.exit`。
3. **跨平台一致（Linux/Windows；x86-64/aarch64/riscv64）**：不硬编码分隔符/换行（用 `File.separator`/`System.lineSeparator()`/nio 路径 API）；不依赖平台字节序/时区库内容（标准 charset 必有）；超越函数断容差而非位相等；不依赖 `System.getenv`/`ProcessBuilder`/平台专属 FS 属性。

## jtreg 约定（同现有）

```java
/*
 * @test
 * @summary <fqcn> conformance: white-box structure + black-box behavior.
 * @run main <Simple>Test
 */
public class <Simple>Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() { /* Class.forName + modifiers/inheritance/members */ }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() { /* 多步组合操作 + 断言 */ }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
```

- 同一文件 `JDK-{27,8}/API/<包路径>/<Simple>/<Simple>Test.java` 内分白盒（前）/黑盒（后）两部分；已有白盒内容保留/补齐。
- 验证用隔离 workDir 跑 jtreg，必须 `Test results: passed: N` 零失败。

## 优先级（按用户暴露度降序，分批推进）

第一批（JDK-27 核心 API）：`java.lang` 字符串/数值/Math、`java.util` 集合与 Map/Set、`java.util.stream`+`Optional`、`java.time`、`java.io`+`nio.file`、`java.nio` buffer/charset。
后续批：`java.util.concurrent`、`java.math`/`java.text`/`java.util.regex`、`java.net`、`java.security`、`java.sql`、`javax.*`、`jdk.*` 导出面。
