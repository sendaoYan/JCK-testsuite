# java.security.PEM 黑盒行为测试设计

## 1. API 功能
- 构造：`PEM(String type, String base64Content)`、`PEM(String type, byte[] content)`
- 查询：`type()`、`subtype()`、`content()`、`encodableName()`

## 2. Java 用户如何使用
统一 PEM 表达：cert/key/CRL 以 (type, DER bytes) 记录，与 PEMEncoder/PEMDecoder 组成 round-trip。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PEM.java`（preview）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PEMDecoder/` 系列预览测试（cert/key/CRL 解码矩阵）

## 4. 测试用例设计 + 自动化实现
- 正常：type/content 透传、equals/hashCode 基于两字段
- 组合：PEM → PEMEncoder.encodeToString → PEMDecoder.decode → equals 往返
- 异常：IAE（坏 base64）；null content NPE

断言入口：`PEMTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化补充（本批）
- **实现方式**：PEM 为 JDK-27 preview API，`import java.security.PEM` 在普通 javac（无 `--enable-preview`）下直接报错；测试改为全反射访问（`Class.forName`/`Method.invoke`），可按套件标准流程编译与 jtreg 运行。
- 深化断言：`content()`/`leadingData()` 每次调用返回新副本；`decode()` 跨调用确定；2 参构造 leadingData=null、显式空数组保长度 0；toString 精确规范形 `-----BEGIN TEST-----\r\naGVsbG8=\r\n-----END TEST-----\r\n`（CRLF+尾 CRLF）；长 body 原样输出不重排；String/byte[] 两种构造渲染一致。
- base64 校验**惰性**：坏内容构造成功、`decode()` 时才 IAE；宽松 MIME 语义（`aGVsbG8!!!` 仍解出 "hello"）。
- 标签校验：仅拒绝 RFC 7468 保留字（`-`开头/`BEGIN `/`END `前缀）；小写、空串、含数字空格均合法（实测记录）。
- **equals/hashCode 为身份语义**（同值不同对象不等；非两字段相等——修正 §4 的推测）；null type/null content → NPE。
