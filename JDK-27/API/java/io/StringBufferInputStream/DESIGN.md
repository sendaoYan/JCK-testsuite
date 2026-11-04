# java.io.StringBufferInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- @Deprecated 字节视图：`(String)`，每字符取低 8 位（忽略高位，非 ASCII 损坏）。
- `read()`/`read(byte[],int,int)`、`skip`、`available()`（全长度）、`reset()`（回起点，无需 mark）、`close()`（无操作）。
- protected buffer/count/pos；无 mark 支持（mark/reset 语义退化为 reset 回 0）。

## 2. Java 用户如何使用
- 遗留 API：ASCII 测试数据字节源；新代码用 ByteArrayInputStream 或 StringReader（正确 Unicode）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 低 8 位截断实现 — `jdk25u-dev/src/java.base/share/classes/java/io/StringBufferInputStream.java`（`buffer[pos++] & 0xFF`...）。

## 4. 测试用例设计 + 自动化实现（StringBufferInputStreamTest.testBlackBox）
1. 顺序/区域/bulk 读与 EOF；skip 夹紧；available 全长度。
2. 'é'(U+00E9) 读出 0xE9——低 8 位截断的实证（非 ASCII 损坏）。
3. reset 无 mark 回起点；close 后仍可读（无失效语义）；空串即 EOF。
- 健壮性: 纯内存。

产出：`StringBufferInputStreamTest.java`（白盒 + 黑盒共 24 断言）。
