# java.net.http.HttpRequest.Builder 设计文档

## 1. API 功能

`HttpRequest` 构造器（`HttpRequest.newBuilder(uri)` / `newBuilder(request, filter)`）。

- URI/方法：`uri`、`GET/POST/PUT/DELETE/HEAD`、`method(String, BodyPublisher)`。
- 头：`header(name, value)` 累加、`setHeader` 替换、`headers(k, v, ...)`。
- 其它：`timeout(Duration)`、`expectContinue(boolean)`、`version(Version)`、
  `setOption(HttpOption<T>, T)`（JDK-27）、`copy()`（快照）、`build()`。
- `build()` 前必须设置 URI，否则 ISE "uri is null"。

## 2. 使用方式

`HttpRequest.newBuilder(URI.create("https://x/")).header("A", "1").POST(BodyPublishers.ofString("b")).build()`。

## 3. 参考来源

- JDK-27 `javap`；`jdk/internal/net/http/HttpRequestBuilderImpl.java`。

## 4. 测试用例设计 + 易错规范细节

文件：`BuilderTest.java`。

- 返回 this：含 HEAD/copy/timeout 等全部方法。
- 负例矩阵：null URI NPE "uri must be non-null"、build() 无 URI ISE、相对/ftp/file/无 host/ws URI IAE、
  header null NPE、非法名（空格/空串）IAE、值含 CR/LF IAE、受限头（Connection/content-length/Host/Upgrade/Expect）IAE、
  HTAB 值合法、headers 奇数个数/空数组/null 元素 NPE、timeout null NPE + ZERO/负 IAE、
  method null/空/CONNECT/"GE T" IAE、POST(null) NPE、version(null) NPE、setOption(null) NPE + 错类型 IAE（probed）。
- 头累加/替换：`header` 多值按序、`setHeader` 整体替换、map 键保留原拼写（大小写敏感键集合）。
- `copy()` 独立性（后续修改不影响快照）、copy-of-copy；`setOption(..., null)` 移除选项（probed）。
- GET/DELETE/HEAD 清除 body；两请求内容相同则 equals。

## 5. 版本差异

- JDK-27：`setOption/copy/HEAD`；JDK 11–17 无 setOption/copy（部分新增于 JDK 16/17）。
