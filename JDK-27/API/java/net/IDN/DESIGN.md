# java.net.IDN 测试设计案（JDK-27）

> 覆盖目标：完整契约。toASCII/toUnicode 国际化标签往返、ACE（xn--）形式、ASCII 透传、
> USE_STD3_ASCII_RULES（首尾连字符与非 LDH ASCII 拒绝）、空标签校验、尾点/根标签、
> ALLOW_UNASSIGNED 标志、null→NPE、非法标签→IllegalArgumentException。

## 1. API 功能

- `toASCII(String)` / `toASCII(String,int)`、`toUnicode(String)` / `toUnicode(String,int)`。
- 标志：`ALLOW_UNASSIGNED`、`USE_STD3_ASCII_RULES`。

## 2. 用户如何使用

- 域名国际化：`IDN.toASCII("bücher.de")` → `xn--bcher-kva.de`，反向 `toUnicode`。
- `USE_STD3_ASCII_RULES` 严格化（拒绝首尾连字符/非 LDH ASCII）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/IDN.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/IDN/`（IllegalArg、UseSTD3ASCIIRules）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/IDN/`。

## 4. 测试用例设计（映射到 IDNTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 标志常量非零且互异 | testFlags |
| P0 | toASCII/toUnicode 往返 + ACE + ASCII 透传 | testToAsciiUnicode |
| P0 | 尾点/根标签合法；表意句号→点 | testTrailingDotAndRoot |
| P0 | 空标签（`com..net` 等）→IAE（有/无 STD3） | testEmptyLabelValidation |
| P0 | STD3：首尾连字符/非 LDH ASCII→IAE；合法 unicode 标签→非空 | testStd3Rules |
| P0 | null→NPE | testNullSemantics |

## 易错规范细节（已锁入断言）

- `toASCII(null)`/`toUnicode(null)` 抛 NPE（含带 flags 的重载）。
- 空标签（连续点、前导/尾随空标签）**无论** STD3 与否都抛 IAE；尾点 `com.` 与根 `.` 合法。
- STD3 额外拒绝：首尾连字符、`,`/`{`/DEL 等非 LDH ASCII；连字符在中间合法。

## 深化补充（批次 E）

### 缺口分析（深化前 37/44 断言：缺精确 ACE 表、标志常量精确值、标签长度边界、toUnicode 永不抛、非 LDH 透传语义）

### 新增维度（→ 方法）

| 维度 | 方法 | 锁入断言 |
|---|---|---|
| 标志常量精确值 | testFlagConstantValues | ALLOW_UNASSIGNED==1、USE_STD3_ASCII_RULES==2 |
| 精确 ACE 表（多语种） | testExactAceTable + testExpandedAceTable | 例え.jp→xn--r8jz45g.jp、日本語.jp→xn--wgv71a119e.jp、münchen.de→xn--mnchen-3ya.de、испытание.рф→xn--80akhbyknj4f.xn--p1ai 等及反向；尾点保持 |
| 大小写处理 | testCaseHandling | ASCII 大小写原样保持；混合大小写 ACE 前缀（XN--BCHER-KVA.DE→BüCHER.DE，探针确定性怪癖） |
| 非 LDH 透传 vs STD3 | testNonLdhWithoutStd3 | 空格/下划线/叹号无 STD3 时**透传不抛**；STD3 时 IAE "Contains non-LDH ASCII characters"（消息精确） |
| 标签长度边界 | testLabelLengthBoundary | 63 字符合法；64 字符 IAE "The label in the input is too long"（消息精确） |
| toUnicode 永不抛 | testToUnicodeNeverThrows | xn--a-ecp.ru/xn--.jp/xn---.jp/xn--a/xn--0.jp/!!invalid!!/空串/根点全部原样返回；单标签合法 ACE 仍解码 |
| 禁用码点 | testProhibitedCodePoints | U+E000（私用区）有无 ALLOW_UNASSIGNED 均 IAE，消息含 "A prohibited code point" |
| STD3 非法字符全表 | testStd3IllegalMatrix | 27 个非 LDH ASCII 逐个 IAE；LDH 合法表逐个通过 |
| 空标签扩展矩阵 | testEmptyLabelMatrixExtended | a..b/../a../..b/.a.b./a.b.. toASCII 全 IAE；**toUnicode 透传不抛**（探针修正） |

### 易错规范细节（新增实证）

- toASCII 无 STD3 时对空格/下划线/叹号**不校验**（透传）；STD3 才拒绝（消息 "Contains non-LDH ASCII characters"）。
- toUnicode 对任何非法输入（含空标签、坏 ACE）都**原样返回、绝不抛**；仅 null 抛 NPE。
- toUnicode 对单标签合法 ACE（xn--bcher-kva→bücher）照常解码。
- ALLOW_UNASSIGNED 并不允许 U+E000 私用区字符（仍 "A prohibited code point"）。
