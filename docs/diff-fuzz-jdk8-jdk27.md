# 差分模糊测试报告：JDK-8 vs JDK-27（文本/二进制输入类）

> 手段：黑盒差分 fuzz（不插桩引导类、不依赖 JQF/Randoop），自研轻量 harness。
> 目标 API：`Integer/Long/Double/Float` 的 parse/decode、`java.util.regex.Pattern`（编译 + 匹配）、`java.text.NumberFormat/DecimalFormat`（固定 `Locale.US`）、`java.net.URI/URL` 解析、`java.util.Properties.load` 转义。
> 日期：2026-11-04。确定性：固定 `Random` 种子，迭代可复现，headless（纯 `java.*`，无 AWT/Swing，无 `@key`）。

## 1. 结论速览

- 规模：**23,343 个用例**（同一批输入在两个 JDK 各跑一遍，共 46,686 次执行）。
- 命中分歧：**133 处**（0.57%），全部可解释，**无候选回归、无平台噪声**。
- 分歧根因只有 6 类，全部落在「JDK 9 起的新增特性 / 文档化行为变更」或「JDK 8 缺陷在 27 中已修复」两桶内：
  - `Pattern` 新增 `\X`（Unicode 扩展字素簇）—— 41 处；
  - `\b`/`\B`/`\w` 改用 Unicode 单词字符定义 —— 3 处；
  - `NumberFormat`/`DecimalFormat` 格式化 `NaN`：JDK 8 输出 `�`（U+FFFD），27 输出 `NaN` —— 30 处；
  - 默认货币负数格式：JDK 8 会计括号 `($X)`，27 减号 `-$X` —— 56 处；
  - `NumberFormat` 大 double 格式化尾数：JDK 8 产生虚假尾数 —— 1 处；
  - `URI` 异常索引 / `URL` 主机名空格校验 —— 各 1 处。
- 零分歧面（强稳定）：`Integer/Long/Double/Float` 的 parse/decode/radix（共 ~8,279 用例）、`Properties.load`（Reader+InputStream 共 ~3,098 用例）、URI/URL 的随机主体（各 1500 用例）均为 **0 分歧**。parse 面 8→27 非常稳定。

## 2. Harness 用法

Harness 位于 `tools/diff_fuzz/`，两个文件：

- `DiffFuzz.java`：单文件、单类，用固定种子 `Random(0x51D1FF2026L)` 确定性生成全部输入，并对每个用例输出一行归一化结果。只依赖两个 JDK 都有的 API（可用 JDK-8 javac 编译，同一 class 在两边运行）。
- `run_diff.py`：编译 + 双 JDK 各跑一遍 + 逐行 diff（跳过 `META` 头行）。

```bash
cd tools/diff_fuzz
python3 run_diff.py \
  --jdk8  /data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/java \
  --jdk27 /data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1/bin/java
```

等价的手工步骤：

```bash
J8=/data/yansendao/JDK/binary/temurin/jdk8u372-b07
J27=/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1

"$J8/bin/javac"  -encoding UTF-8 -d out DiffFuzz.java     # 用 JDK-8 编译，class 两边通用
"$J8/bin/java"   -Dfile.encoding=UTF-8 -cp out DiffFuzz > out/jdk8.txt
"$J27/bin/java"  -Dfile.encoding=UTF-8 -cp out DiffFuzz > out/jdk27.txt
# 逐行 diff，跳过 META 行；结果落 out/diff.txt 与 out/diff.json
```

产物（`tools/diff_fuzz/out/`）：`jdk8.txt`、`jdk27.txt`、`diff.txt`、`diff.json`。

## 3. 规模与确定性

| 类别 | 用例数 | 分歧数 |
|---|---:|---:|
| `Integer.parseInt` / `decode` / `parseInt-radix` | 1547 / 847 / 800 | 0 / 0 / 0 |
| `Long.parseLong` / `decode` / `parseLong-radix` | 1547 / 847 / 800 | 0 / 0 / 0 |
| `Double.parseDouble` / `valueOf` | 2056 / 1256 | 0 / 0 |
| `Float.parseFloat` | 526 | 0 |
| `Pattern.compile` / `-ci` / `-uc` / `-rand` | 92 / 92 / 92 / 600 | 1 / 1 / 1 / 13 |
| `Regex.match-seed` / `-rand` | 1104 / 600 | 15 / 13 |
| `NumberFormat.parse` / `format` / `format-rand` | 2400 / 72 / 400 | 0 / 9 / 52 |
| `DecimalFormat.parse` / `-rand` / `format` | 260 / 1040 / 182 | 13 / 0 / 13 |
| `URI` / `URI-rand` | 51 / 1500 | 1 / 0 |
| `URL` / `URL-rand` | 34 / 1500 | 1 / 0 |
| `Properties.load-reader` / `-stream` / rand×2 | 49 / 49 / 1500 / 1500 | 0 / 0 / 0 / 0 |
| **合计** | **23,343** | **133** |

确定性来源：`java.util.Random` 的算法在规范里被明确指定（LCG），JDK-8 与 JDK-27 产生**完全相同的输入序列**，因此无需把输入外置成文件，也无需跨进程对齐；两个 run 逐行一一对应，只有真正行为不同才会让某一行不同。种子 `0x51D1FF2026L`（十进制 351415509030）写在 `DiffFuzz.SEED`，改它即可换一批输入、结果稳定可复现。

## 4. 归一化方式

Harness 只输出可比较的「语义信号」，把版本噪声降到最低：

- 每条输出为 `类别<TAB>序号<TAB>转义后输入<TAB>转义后结果`。
- 异常只取**异常类名**（`EXC=<SimpleName>`），不取异常 message（message 常含版本串/实现细节，属于噪声）。`URISyntaxException` 额外保留 `getReason()` 与 `getIndex()`。
- 数值 parse 成功输出规范值：`Integer/Long` 输出十进制，`Double`/`Float` 输出 `doubleToLongBits`/`floatToIntBits` 的十六进制（精确到 bit，含 `-0.0` 符号）。
- `NumberFormat/DecimalFormat.parse` 同时记录 `parse(String)` 结果与 `parse(String, ParsePosition)` 的**推进位置**（`@<index>`），比单看成功/失败多一层信号。
- `Properties` 加载后把键**排序**后输出 `k=v`（Hashtable 迭代顺序未定义，必须排序才确定）。
- 输入/结果统一做单行转义：`\`→`\\`、`\t`→`\t`、`\n`→`\n`、`\r`→`\r`、控制字符→`\xNN`，避免字段里出现真实 TAB/换行导致行级错位。

## 5. 命中分歧明细与归类

全部 133 处。按根因归类：

### 5.1 `Pattern` 新增 `\X`（41 处）—— 规范允许（JDK 9 新增特性）

- 输入：正则 `\X`（Unicode 扩展字素簇匹配）。
- JDK-8：`Pattern.compile("\\X")` 抛 `PatternSyntaxException`。
- JDK-27：编译通过，`\X` 匹配一个扩展字素簇（grapheme cluster）。
- 归类：**规范允许**。`\X` 是 JDK 9 起才在 `Pattern` 文档里正式定义的转义，8 不支持属预期。41 处全部来自该转义本身（种子 1×3 编译 + 12 匹配）及随机正则里混入 `\X` 的片段（13 编译 + 13 匹配）。

### 5.2 `\b`/`\B`/`\w` Unicode 单词字符（3 处）—— 规范允许（文档化行为变更）

- 输入：`pat=\B`、`subj=İı`（U+0130 U+0131）。
- JDK-8：`find=true|start=1|end=1`；JDK-27：`find=true|start=0|end=0`。
- 另两例：`subj=Aé`（8: `start=1`；27: `start=2`）、`subj=中文`（8: `start=1`；27: `start=0`）。
- 归类：**规范允许**。JDK 9 起 `\w`/`\W`/`\b`/`\B` 按 `Character.isLetterOrDigit` 把非 ASCII 字母/数字也算作「单词字符」，因此非 ASCII 文本的单词边界位置漂移。属于文档化的行为变更，但值得在 regex 行为测试里补一条双 JDK 断言（对应本项目「API 行为测试」要覆盖的面）。

### 5.3 格式化 `NaN`：JDK 8 输出 `�`，27 输出 `NaN`（30 处）—— 规范允许（JDK 8 缺陷已修）

- 输入：`NumberFormat.getNumberInstance(Locale.US).format(Double.NaN)`（及 integer/currency/percent 三变体）、`DecimalFormat("0.00").format(NaN)`、`DecimalFormat("0.00").parse("NaN")`。
- JDK-8：`format` 输出 `�`（U+FFFD 替换符）；`parse("NaN")` 抛 `ParseException`。
- JDK-27：`format` 输出 `NaN`；`parse("NaN")` 返回 `Double.NaN`（`0x7ff8000000000000`，parse 位置推进到 3）。
- 归类：**规范允许**（JDK 8 缺陷，27 按规范修复）。`DecimalFormatSymbols` 的 NaN 符号本就应是 `NaN`，8 的 `DigitList` 把 NaN 处理成 U+FFFD 属 bug；`DecimalFormat.parse("NaN")` 在 27 才接受 NaN 字面量。这是本项目「dual-JDK gotcha」里需要记的一笔：**任何涉及 NaN 格式化的测试，8 与 27 结果不同**。

### 5.4 默认货币负数：`($X)` vs `-$X`（56 处）—— 规范允许（JDK 9 文档化变更）

- 输入：`NumberFormat.getCurrencyInstance(Locale.US).format(-42.0)`、以及大量随机负 double。
- JDK-8：会计括号 `($42.00)`、`($0.00)`、`($∞)`。
- JDK-27：减号 `-$42.00`、`-$0.00`、`-$∞`。
- 归类：**规范允许**。JDK 9 起默认货币负数的 negative 子模式由会计括号改为减号（这是 JDK 9 明确的行为变更）。涉及 5 个固定值 + 51 个随机值（`NumberFormat.format`/`format-rand`）。

### 5.5 `NumberFormat` 大 double 格式化尾数（1 处）—— 规范允许（JDK 8 缺陷已修）

- 输入：`NumberFormat.getNumberInstance(Locale.US).format(-3.99290199472105e18)`（bits `c3cbb4d19abd25ff`，精确值 `-3992901994721050112`）。
- JDK-8：`-3,992,901,994,721,050,100`（尾数 `...100`，是虚假的、不满足最短往返的位数）。
- JDK-27：`-3,992,901,994,721,050,000`（等于 `Double.toString` 的最短往返表示）。
- 归类：**规范允许**（JDK 8 缺陷，27 修复）。JDK 8 的 double→十进制位数转换算法对部分大 double 会生成虚假尾数；27 用最短表示后正确。仅 1 处命中（随机 double 中恰有此量级）。

### 5.6 `URI` 异常索引与 `URL` 主机名空格（各 1 处）—— 规范允许（加固/精度修正）

- 输入：`new URI("http://host with space/")`。
  - JDK-8：`URISyntaxException` `idx=7`（指向 host 首字符，位置不准）。
  - JDK-27：`URISyntaxException` `idx=11`（指向空格本身，正确）。
- 输入：`new URL("http://exa mple.com")`。
  - JDK-8：解析成功，`host=exa mple.com`。
  - JDK-27：抛 `MalformedURLException`（拒绝主机名中的空格）。
- 归类：**规范允许**（JDK 8 缺陷/加固，27 更严格）。两者异常类型层面只有 URL 从「成功」变「抛异常」，属解析器加固；URI 仅异常索引字段修正，异常类型一致。

## 6. 分类汇总

| 归类 | 根因 | 分歧数 | 备注 |
|---|---|---:|---|
| 规范允许（JDK 9 新增特性） | `\X` 扩展字素簇 | 41 | 8 无此转义 |
| 规范允许（文档化行为变更） | `\b`/`\B`/`\w` Unicode | 3 | 非 ASCII 单词边界漂移 |
| 规范允许（JDK 8 缺陷已修） | NaN 格式化/解析 | 30 | 8 输出 U+FFFD、parse 拒 NaN |
| 规范允许（JDK 9 文档化变更） | 货币负数默认格式 | 56 | 括号 → 减号 |
| 规范允许（JDK 8 缺陷已修） | 大 double 格式化尾数 | 1 | 8 虚假尾数 |
| 规范允许（加固/精度修正） | URI 异常索引 / URL 空格 | 2 | 27 更严格/更准 |
| **候选回归** | — | **0** | 未发现 27 相对 8 的疑似回归 |
| **平台噪声** | — | **0** | 全部可归因，无环境/版本串噪声 |

## 7. 关键 gotcha（写测试/继续扩面时注意）

1. **同一 class 双跑**：必须用 JDK-8 javac 编译 harness，8 的 class 27 能跑、27 的 class 8 跑不了（major version 差异）。
2. **`Random` 序列规范稳定**：`java.util.Random` 的 LCG 算法是规范指定，8/27 生成相同输入，无需外置输入文件或跨进程对齐。
3. **转义是 diff 正确性的关键**：输入/结果里真实 TAB/换行/NUL 必须转成 `\t`/`\n`/`\xNN`，否则字段错位会造成大量假分歧；本 harness 的分歧数 133 是在全转义下得到的干净数。
4. **异常只比类型、不比 message**：message 常含实现细节，是噪声。但 `URISyntaxException` 的 `getIndex()` 值得单独比（本次就抓出 1 处索引修正）。
5. **`NumberFormat`/`DecimalFormat` 的 NaN 与货币负数是 dual-JDK 双跑必炸点**：任何行为测试若断言 `format(NaN)` 或 `format(-x)`（货币）的字符串，8 与 27 必然不同，需按版本分叉或只断言数值语义。
6. **`Properties.load` 畸形 `\u` 转义两版一致**：本 harness 实测 `key=\u12`、`key=\uZZZZ` 在 8u372 与 27 上**都抛 `IllegalArgumentException`**（Reader 与 InputStream 两条路径均是），0 分歧。常见「JDK 9 才拒绝畸形 \u」的说法对本 JDK-8 发行版不成立，勿据此写分叉。
7. **parse 面极稳**：`Integer/Long/Double/Float` 的 parse/decode/radix 全量 0 分歧，说明 8→27 数值解析语义完全兼容；后续扩面可把精力放在 regex 与格式化/解析定位这类「文档化漂移」面，而不是数值 parse。

## 8. 落点建议

- 这 133 处全部是「确定性输入 + 双 JDK 行为」的现成素材，可直接转成 jtreg 差分断言（本项目行为测试的一部分）：对 5.2–5.5 的四类行为漂移各写一条「JDK-8 预期 vs JDK-27 预期」双分支断言。
- 扩面方向（按投入产出）：`DateFormat/MessageFormat/NumberFormat` 更多 Locale 与 pattern、`Integer/Long` 的 `parseUnsignedInt/parseUnsignedLong`（27 与 8 都有）、`Integer.toString(radix)` 往返、`java.time` 与 `SimpleDateFormat`（但 27 才有的 API 跳过不报）。
