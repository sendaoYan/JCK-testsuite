# java.util.Base64 测试设计

## 1. API 功能
- 编码器：`getEncoder`、`getUrlEncoder`、`getMimeEncoder([lineLength, sep])`，每个有 `encodeToString`/`encode`/`wrap`/`withoutPadding`。
- 解码器：`getDecoder`、`getUrlDecoder`、`getMimeDecoder`，每个有 `decode(byte[]/String)`/`wrap`。

## 2. Java 用户如何使用
- 标准编码：`Base64.getEncoder().encodeToString(bytes)`。
- URL 安全：`Base64.getUrlEncoder()`。
- MIME 长文本：`Base64.getMimeEncoder()`。
- 无填充：`Base64.getEncoder().withoutPadding()`。
- 流式：`Base64.getEncoder().wrap(os)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/security/PEMDecoder.java`：`Base64.getMimeDecoder` 解析 PEM。
- JDK 源码 `sun/net/www/protocol/http/BasicAuthentication.java`：`Base64.getEncoder` 编码凭据。
- JDK 源码 `sun/net/www/protocol/http/NegotiateAuthentication.java`：Base64 编解码。
- 广泛用法：JWT、Basic Auth、数据 URL。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、六个工厂方法存在。
- 黑盒（固定字节）：
  - 基础编码 `aGVsbG8=` 与往返、空输入。
  - URL 安全 `__4Afw==` 往返、withoutPadding 去 `=` 且可解码。
  - MIME 长输入往返、`wrap` 流式编码到 `ByteArrayOutputStream`。
  - 非法输入解码抛 IllegalArgumentException。
