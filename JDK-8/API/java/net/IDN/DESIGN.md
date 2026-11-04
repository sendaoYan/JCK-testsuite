# java.net.IDN 测试设计案（JDK-8）

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
