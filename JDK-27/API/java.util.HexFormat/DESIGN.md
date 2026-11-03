# java.util.HexFormat — 测试设计案

## 1. API 功能
十六进制转换工具类（JDK 17+）：字节数组/基本类型与 hex 字符串互转、自定义分隔/前后缀/大小写。final、不可变。

- 工厂：of()/ofDelimiter(String)；变体：withDelimiter/withPrefix/withSuffix/withUpperCase/withLowerCase。
- 转换：formatHex/parseHex/toHexDigits/fromHexDigits/fromHexDigitsToLong/isHexDigit/fromHexDigit。

## 2. Java 用户如何使用
`HexFormat.of().formatHex(bytes)` 转 hex；`parseHex(str)` 转回字节；`withDelimiter(":")` 加分隔。
链路：of -> formatHex -> parseHex；非法输入抛 IAE/NFE。

## 3. 开源代码实际用法调研
- OpenJDK `HexFormat`（本地 `~/git/jdk25u-dev`）：formatHex 默认小写无分隔；
  parseHex 奇数长度抛 IllegalArgumentException、非 hex 字符抛 NumberFormatException——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/HexFormat/`.

## 4. 测试用例设计 + 自动化实现（HexFormatTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| formatHex | 小写、大小写变体、范围 |
| 自定义 | delimiter/prefix/suffix |
| parseHex | 往返、奇数 IAE、非法字符 NFE |
| 数字转换 | fromHexDigits/toHexDigits/toHighHexDigit |
| 静态 | isHexDigit/fromHexDigit |

共 5 组场景、约 16 个 check（`testBlackBox()`）。
