# java.net.http.HttpRequest 设计文档

## 1. API 功能

HTTP 请求的不可变抽象表示。

- 工厂：`newBuilder()`（无参）、`newBuilder(URI)`、`newBuilder(HttpRequest, BiPredicate)`（过滤复制）。
- 查询：`method()`、`uri()`、`headers()`、`bodyPublisher()`、`timeout()`、`expectContinue()`、`version()`、`getOption(HttpOption)`。
- 相等性：`equals`/`hashCode`（final）。
- Builder：`uri`/`expectContinue`/`version`/`header`/`headers`/`setHeader`/`timeout`/`GET`/`POST`/`PUT`/`DELETE`/`HEAD`/`method`/`setOption`/`copy`/`build`。
- BodyPublisher：`BodyPublishers.noBody/ofString/ofByteArray/...`，`contentLength()` 报告字节数。

## 2. Java 用户如何使用

- 组合链路：`HttpRequest.newBuilder(uri).header("Accept","text/plain").timeout(d).GET().build()`
  → `client.send(req, BodyHandlers.ofString())`。
- 请求体：`BodyPublishers.ofString("body")` → `contentLength()`；`method("PATCH", noBody())` 自定义方法。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/java/net/http/HttpRequest.java`）：
  `header` 累积、`setHeader` 覆盖、`headers(String...)` 成对写入；`equals/hashCode` 按 method/uri/headers/body 等比较。

## 4. 测试用例设计 + 自动化实现

文件：`HttpRequestTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / protected 无参构造器 / 关键方法面。
- 黑盒：`newBuilder(URI)` 默认值 → 方法选择助手（GET/DELETE/HEAD/POST/PUT/method）→
  BodyPublisher `contentLength` 组合 → 头累积/覆盖/成对 → timeout/version/expectContinue/setOption 读回 →
  无参 `newBuilder().uri()` → `equals/hashCode` → 复制与过滤复制。

## 6. 缺口分析（本批深化）

原 51 条断言无系统负例矩阵。本批追加至 129 条：

- 负例矩阵（精确消息）：newBuilder(null) NPE "uri must be non-null"、build() ISE "uri is null"、
  相对/ftp/file/无 host/ws URI IAE、header null NPE、非法名/CR/LF IAE、5 个受限头 IAE、HTAB 合法、
  headers 奇数/null NPE、timeout null/ZERO/负、method null/空/CONNECT/"GE T"、POST(null)、
  setOption(null) NPE + 错类型 IAE（probed）。
- 方法-体交互：GET/DELETE/HEAD 清除 body；build 两次 equals；copy() 快照独立性。
- equals 敏感性：忽略 timeout/expectContinue/version/options；反映 uri/method/headers/body；
  头插入顺序无关但多值顺序有关。
- 不可变性：map/allValues/entrySet 5 处 UOE。
- copy 过滤器语义：过滤器按头值逐个调用 [X=1,X=2,Y=3]；多值保留；选项保留（probed）。
