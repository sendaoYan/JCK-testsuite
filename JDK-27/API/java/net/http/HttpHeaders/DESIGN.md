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

## 6. 缺口分析（本批深化）

原 23 条断言未覆盖过滤语义与规范化。本批追加至 152 条：

- 构造负例（probed）：null map/filter/key/值列表/列表内 null NPE；空/全空白名 IAE "empty key"；
  大小写重复键 IAE "duplicate key: x" 且判重发生在过滤之前；过滤器抛异常同步传播。
- 规范化（probed）：键值两侧 trim（混合逐值 trim）；空白名 trim 后空 → IAE；空值列表键消失；
  空串值保留；of() 不做字符集校验（空格名/LF 值/非 ASCII 名/HTAB 名均接受）。
- 过滤语义（probed）：过滤器按 (name, value) 逐对调用（A=1,A=2,B=3）；丢弃单值保留其余；
  全丢弃则键消失；过滤器看到 trim 后的名值与原大小写；空值列表不过滤。
- firstValueAsLong（probed）：+42/trim 后解析成功；空串/文本/溢出/十六进制/下划线/中嵌空格 NFE
  （精确 message "For input string: X"）；仅取首值；null key NPE。
- 大小写不敏感：firstValue/allValues/firstValueAsLong/map()（containsKey/get 任意大小写）。
- map() 视图：键排序、UnmodifiableMap/Entry 类名、12 处 UOE（put/remove/clear/keySet 迭代器/
  entrySet add-remove/values add/值 set 等）、map()/allValues() 稳定实例。
- equals/hashCode：键大小写无关、值顺序敏感、空 headers 为缓存单例且 hashCode=0（probed）、
  toString 格式 `java.net.http.HttpHeaders@N { {K=[..]} }`。
- 快照隔离：of() 后修改源 map/源列表不可见（probed）；不可序列化（probed）。
