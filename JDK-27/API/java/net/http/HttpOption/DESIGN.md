# java.net.http.HttpOption 设计文档

## 1. API 功能

泛型接口 `HttpOption<T>` 描述可配置到 `HttpRequest.Builder` 上的命名选项。

- 常量：`H3_DISCOVERY`（`HttpOption<Http3DiscoveryMode>`），控制 HTTP/3 发现行为。
- 方法：`name()`（选项名）、`type()`（选项值的 `Class<T>`）。
- 嵌套枚举：`HttpOption.Http3DiscoveryMode`：`ANY` / `ALT_SVC` / `HTTP_3_URI_ONLY`。

## 2. Java 用户如何使用

- 组合链路：`HttpRequest.newBuilder(uri).setOption(HttpOption.H3_DISCOVERY, HttpOption.Http3DiscoveryMode.ANY).build()`
  → `request.getOption(HttpOption.H3_DISCOVERY)` 读回。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/java/net/http/HttpRequest.java` 与 `HttpOption.java`）：
  `Builder.setOption`/`HttpRequest.getOption` 围绕 `HttpOption` 泛型读写选项；`H3_DISCOVERY` 是当前唯一的公开选项。

## 4. 测试用例设计 + 自动化实现

文件：`HttpOptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public interface / 一个类型参数 / `H3_DISCOVERY` 常量。
- 黑盒：常量 `name()`/`type()`/`instanceof HttpOption`；`Http3DiscoveryMode` 三个枚举值 + `valueOf`。

## 6. 缺口分析（本批深化）

原 12 条断言仅核对常量与枚举存在性。本批追加至 42 条：

- sealed 形状（probed）：isSealed true；permits 恰 java.net.http.HttpRequestOptionImpl；
  恰两个抽象方法 name()/type() 及其返回类型；H3_DISCOVERY 为
  `HttpOption<Http3DiscoveryMode>` 类型、public static final、toString 为名字。
- setOption 集成（probed）：默认缺省；ALT_SVC round-trip；覆盖写；setOption(..., null) 移除；
  重设恢复；Builder.copy() 携带；request.getOption() 读回。
- 负例（probed）：setOption(null, v) NPE；错类型值 IAE；getOption(null) NPE；缺省 equals Optional.empty。
