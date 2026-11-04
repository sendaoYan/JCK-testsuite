# java.nio.charset.Charset 测试方案

## 1. API 功能
- 查找: `forName(String)`, `forName(String,Charset)`, `isSupported(String)`, `availableCharsets()`, `defaultCharset()`
- 身份: `name()`, `aliases()`, `displayName()`, `displayName(Locale)`, `isRegistered()`, `equals`, `hashCode`, `compareTo`, `toString`
- 关系: `contains(Charset)`
- 编解码: `newEncoder()`, `newDecoder()`, `canEncode()`, `encode(String)`, `encode(CharBuffer)`, `decode(ByteBuffer)`

## 2. Java 用户如何使用
- 查找: `Charset.forName("UTF-8")` (大小写/别名归一化), `forName(name, fallback)` 回落.
- 组合链路: `forName -> encode(String) 得 ByteBuffer -> decode(ByteBuffer) 还原 CharBuffer`.
- 关系: `UTF_8.contains(US_ASCII)` 判断字符集子集.

## 3. 开源代码实际用法调研
- `test/jdk/java/net/ProxySelector/LoopbackAddresses.java`: `Charset.forName("UTF-8")` — 直接取标准字符集.
- `test/jdk/tools/jpackage/helpers/jdk/jpackage/test/MsiDatabase.java`: `Charset.forName(header[0])` — 按名字动态取字符集.
- `test/jdk/tools/launcher/DisableBestFitMappingTest.java`: `Charset.forName(System.getProperty("native.encoding")).newEncoder()` — forName + newEncoder 组合.

## 4. 测试用例设计 + 自动化实现
推导: 查找 (大小写/别名/fallback/isSupported/availableCharsets)、身份 (equals/hashCode/compareTo/
toString/displayName/isRegistered)、contains、编解码往返 (ASCII/CJK/代理对)、decode 尊重 remaining
区间、异常 (UnsupportedCharsetException/IllegalCharsetNameException/CharacterCodingException)。
实现于 `CharsetTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 只测标准/必有字符集 (UTF-8/US-ASCII/UTF-16/ISO-8859-1), 不测冷门 charset;
defaultCharset 只断言非空不绑具体值; 中文/emoji 用 Unicode 字面量固定; 无墙钟/随机/哈希顺序/
全局状态/文件/网络/线程依赖.
