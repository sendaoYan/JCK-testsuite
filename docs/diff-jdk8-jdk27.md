# JDK-8 vs JDK-27 行为差分清单

> 生成方式：`tools/diff_jdk8_27/diff_behavior.py` 对 `tools/diff_jdk8_27/probes/` 下每个探针源，
> 分别用 JDK-8（Temurin 1.8.0_372）与 JDK-27（Temurin 27.0.1）编译 + 运行，归一化后 diff。
> 判定为人工复核结论，仅供参考。

- 探针总数：**35**
- 结果分布：一致 30、分歧 4、规范新增 1、规范移除 1、双方编译失败 0、运行错误 0
- 分歧总行数：**5**（全部判定为「规范允许进化」，**0 条候选回归**）

---

## 1. Harness 用法

```bash
# 全量运行，写 tools/diff_jdk8_27/results.json + 控制台汇总
python3 tools/diff_jdk8_27/diff_behavior.py

# 只跑单个探针（stem 不带 .java）
python3 tools/diff_jdk8_27/diff_behavior.py --probe DoubleToStringProbe

# 另存一份 markdown 骨架
python3 tools/diff_jdk8_27/diff_behavior.py --md docs/diff-jdk8-jdk27.md
```

- JDK 路径硬编码在 `diff_behavior.py` 顶部 `JDKS` 字典里（JDK-8 与 JDK-27 的 `java`/`javac`）。
- 探针约定：每个 `probes/*.java` 是一个 `public class`（类名 == 文件名 stem），`main` 输出若干
  `key=value` 行；每个用例用 `ex(key, lambda)` 包裹，抛异常时输出 `key=exception=<全限定异常类名>`；
  绝不让异常逃出 `main`。
- 归一化：stdout 按行切分、去首尾空白、去空行、`sort + dedupe`。即输出**与顺序无关**，任何
  `HashMap`/集合迭代顺序、`Properties` 条目顺序都被抵消。探针本身不产出时间戳/随机数/对象
  identity，因此不再需要额外的正则去噪（本轮 stderr 全空，也无 JVM 警告泄漏）。

---

## 2. 分歧清单

### 2.1 `java.lang.Double.toString(double)` — 最短表示算法（Ryu）

| 项 | 值 |
|---|---|
| 类/方法 | `java.lang.Double.toString(double)` |
| 输入 | `1e23`（即 `100000000000000000000000.0` 的最接近 double） |
| JDK-8 输出 | `9.999999999999999E22` |
| JDK-27 输出 | `1.0E23` |
| 判定 | **规范允许进化**（有意变更，JDK 19 起改用 Ryu 最短表示算法） |

说明：JDK-8 与 JDK-27 的 double 位模式相同，`Double.toString` 的契约只要求「能唯一还原该
double 的最短十进制串」，不承诺具体字符串。`9.999999999999999E22` 与 `1.0E23` 都 round-trip
到同一 double，Ryu 在部分输入上产出更短/不同的形式。这是 **JDK 19 引入的已知行为变化**，非回归。

### 2.2 `java.lang.Math.ulp(double)` — 同根因（仅 toString 呈现不同）

| 项 | 值 |
|---|---|
| 类/方法 | `java.lang.Math.ulp(double)`（返回值经 `Double.toString` 打印） |
| 输入 | `Double.MAX_VALUE` |
| JDK-8 输出 | `1.9958403095347198E292` |
| JDK-27 输出 | `1.99584030953472E292` |
| 判定 | **规范允许进化**（与 2.1 同一 Ryu 根因；`Math.ulp` 的 double 值本身未变） |

说明：`Math.ulp(Double.MAX_VALUE)` 返回的 double 在两版本完全一致，差异只来自把它转成字符串
时的最短表示算法。**不要把这条误报成 `Math.ulp` 行为漂移。**

### 2.3 `java.text.NumberFormat.getCurrencyInstance(Locale.US)` — 负数货币格式

| 项 | 值 |
|---|---|
| 类/方法 | `NumberFormat.getCurrencyInstance(Locale.US).format(double)` |
| 输入 | `-1234.56` |
| JDK-8 输出 | `($1,234.56)` |
| JDK-27 输出 | `-$1,234.56` |
| 判定 | **规范允许进化**（locale 数据升级） |

说明：美国货币负数格式由「括号（accounting style）」变为「前导负号」，属 locale/CLDR 数据演进，
数值等价。locale 相关输出历来允许随数据版本变化。

### 2.4 `java.text.NumberFormat.getNumberInstance(Locale.FRANCE)` — 分组分隔符码位

| 项 | 值 |
|---|---|
| 类/方法 | `NumberFormat.getNumberInstance(Locale.FRANCE).format(double)` |
| 输入 | `1234567.89` |
| JDK-8 输出 | `1 234 567,89`（分组符 U+0020 SPACE） |
| JDK-27 输出 | `1 234 567,89`（分组符 U+202F NARROW NO-BREAK SPACE） |
| 判定 | **规范允许进化**（CLDR 数据升级） |

说明：两组输出「看起来一样」，差异在分组分隔符的 Unicode 码位（U+0020 → U+202F）。这也是
CLDR 对法语区域的分组符规范变更，属 locale 数据演进。视觉上极易漏判，diff 时按字节/码位而非
肉眼看才抓得到。

### 2.5 `java.util.TimeZone.getAvailableIDs()` — tzdb 数据版本

| 项 | 值 |
|---|---|
| 类/方法 | `TimeZone.getAvailableIDs()` |
| 输入 | （无参数，取返回数组长度） |
| JDK-8 输出 | `631` |
| JDK-27 输出 | `632` |
| 判定 | **规范允许进化**（tzdb 升级新增 zone ID） |

说明：时区数据库（tzdb）随 JDK 更新，可用 zone ID 数量变化属预期，不应作为回归。

---

## 3. 规范新增 / 规范移除（签名本就不同，不报 bug）

| 探针 | 类别 | 说明 |
|---|---|---|
| `StringNewApiProbe` | 规范新增 | `String.isBlank()/strip()/stripLeading()/stripTrailing()/repeat()/indent()` 在 JDK-8 编译失败、JDK-27 可用（Java 11+ 新增） |
| `ThreadRemovedApiProbe` | 规范移除 | `Thread.destroy()/stop()/suspend()/resume()/countStackFrames()` 在 JDK-8 存在（运行抛 `UnsupportedOperationException`/`NoSuchMethodError`）、JDK-27 编译失败（JDK 20 移除） |

这两类是「签名存在性差异」，按约定跳过并归类，**不计入行为分歧**。

---

## 4. 一致区（30 个探针，行为无漂移）

`ArraysProbe` `BigDecimalConstructProbe` `BigDecimalProbe` `BigIntegerProbe`
`BinarySearchProbe` `BooleanProbe` `CalendarProbe` `CharacterProbe` `CollectionSortProbe`
`DateProbe` `DecimalFormatProbe` `DoubleParseProbe` `IntegerParseProbe` `LongParseProbe`
`MapProbe` `PropertiesProbe` `RegexGroupNameProbe` `RegexLookbehindProbe` `RegexQuantifierProbe`
`RegexSplitProbe` `SimpleDateFormatProbe` `StringFormatProbe` `StringGetBytesProbe`
`StringIndexOfProbe` `StringSplitProbe` `StringSubstringProbe` `StringToLowerCaseProbe`
`TimeZoneProbe`（除 availableIds 计数外）`URIProbe` `URLProbe`

覆盖范围：`Integer/Long/Double` parse 边界、`String`（substring/indexOf/toLowerCase/format/split/getBytes）、
`BigDecimal`（舍入模式×标度×值域、构造）、`BigInteger`、集合 sort/二分/Map、`java.util.regex`
（lookbehind/组名/量词边界/split）、`NumberFormat/DecimalFormat/SimpleDateFormat`、`Date/Calendar/TimeZone`、
`URI/URL`、`Properties` 转义——这些稳定契约在 8 与 27 之间表现一致。

---

## 5. 假阳性来源与归一化手段

本轮未出现假阳性；为防未来引入，记录已知噪声源与对策：

| 噪声源 | 对策 |
|---|---|
| `HashMap`/集合迭代顺序 | 探针用 `key=value` 且 key 唯一；harness `sort + dedupe` 抵消顺序 |
| `Properties.store` 时间戳注释行 | 探针内过滤 `#` 开头的行（`store` 会写一条含墙钟时间的注释） |
| `Thread`/对象 identity 的 `@hexhash` | 探针不打印对象 `toString`（除 `Date`/集合等有稳定 `toString` 契约的类型） |
| 墙钟 / 随机 / 默认 TZ / 默认 Locale / 默认 charset | 探针 `main` 开头 `Locale.setDefault(Locale.US)` + `TimeZone.setDefault(GMT)`；harness `java -Dfile.encoding=UTF-8` |
| JVM 版本告警 / stderr 栈迹 | 探针让所有异常落成 `key=exception=<type>`；harness 只 diff stdout，并校验 stderr 为空 |
| 会指数回溯的正则 | 探针里的 `(a+)+` 用例输入收敛到 16 个字符，避免 JDK-8 正则引擎卡死 |

---

## 6. 关键 gotcha

1. **`Double.toString` 在 JDK 19+ 换了算法**：任何「打印 double」的断言（含 `Math.*`、
   `BigDecimal(double)` 之外的浮点输出）都可能跨 8/27 不同；判回归前先确认底层 double 位模式
   是否相同——相同则只算字符串呈现差异。
2. **locale 数据会漂**：货币负号、分组分隔符（如法语 U+0020→U+202F）这类「肉眼看一样」的差异，
   必须按码位 diff，否则漏报。
3. **`TimeZone.getTimeZone(null)` 有重载歧义**：`getTimeZone(String)` 与 `getTimeZone(ZoneId)`
   对 `null` 二义，需显式 `(String) null` 才能两 JDK 都编译。
4. **不要对内部类 toString**：`Calendar.toString()` 会打印 `sun.util.calendar.ZoneInfo[...]`
   内部类名，属非稳定契约，探针一律改取 `get(Calendar.FIELD)` 字段。
5. **签名差异先分流**：`spec_added`/`spec_removed`（一侧编译失败）与行为分歧是两回事，前者按约定
   归类，别当成 bug。
