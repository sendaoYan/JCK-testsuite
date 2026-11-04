# java.net.http.HttpHeaders 设计文档

## 1. API 功能

HTTP 头集合的不可变、大小写不敏感视图，final 类。

- 工厂：`of(Map<String,List<String>>, BiPredicate<String,String> filter)`（结构复制并按 filter 过滤）。
- 查询：`firstValue(String)`（`Optional<String>`）、`allValues(String)`（`List<String>`）、
  `firstValueAsLong(String)`（`OptionalLong`）、`map()`（不可变多值 Map 视图）。
- 转换/其它：`toString()`、`equals`/`hashCode`（大小写不敏感）。

## 2. Java 用户如何使用

- 组合链路：`HttpResponse.headers()` → `firstValue("Content-Type")` / `firstValueAsLong("Content-Length")` / `allValues("Set-Cookie")`。
- 构造：`HttpHeaders.of(map, (name, value) -> predicate)` 过滤敏感头。
- 边界：`map()` 返回不可变视图；重复键（忽略大小写）抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/java/net/http/HttpHeaders.java`）：
  `headersOf` 用 `TreeMap(CASE_INSENSITIVE_ORDER)` 去重/排序，`unmodifiableMap` 包装；
  `HttpResponse`/`HttpClient` 内部多处使用 `firstValue`/`allValues` 读取响应头。

## 4. 测试用例设计 + 自动化实现

文件：`HttpHeadersTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / final / 父类 `Object`。
- 黑盒：构造→`firstValue`/`allValues`/`firstValueAsLong`→大小写不敏感→缺失值行为→非数字值异常→
  `map()` 不可变→`toString`→`equals/hashCode`→filter 过滤→空集合→重复键异常。
