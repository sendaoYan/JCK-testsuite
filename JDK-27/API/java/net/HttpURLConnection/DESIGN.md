# HttpURLConnection 设计文档

## 一、API 功能

URLConnection 的 HTTP/HTTPS 专用子类，提供请求方法、响应码、重定向、跟随跳转、断开连接等 HTTP 语义。

## 二、Java 用户如何使用

用户把 URL.openConnection() 结果强转为 HttpURLConnection，调用 setRequestMethod/getResponseCode/getHeaderField/disconnect() 做 HTTP 请求。

## 三、开源代码实际用法调研

Apache HttpClient 之前的 JDK 原生 HTTP 客户端即为 HttpURLConnection，被无数轻量客户端与工具类使用。

## 四、测试用例设计

验证为 abstract 且继承 URLConnection；静态常量 HTTP_OK=200 等；子类实例上 setRequestMethod/getRequestMethod、followRedirects 默认与设置。

---

## 批次 C 深化（URLConnection 簇）

> 原测试 ~10 断言。本批补齐：36 个 HTTP_* 常量精确值全表、setRequestMethod 合法/非法/null/
> 已连接矩阵（注意 null → ProtocolException 而非 NPE）、流式模式互斥状态机、重定向静态/实例
> 联动、最小子类 getResponseCode/Message → UnknownServiceException、权限、disconnect/usingProxy
> 委托、JDK 9+ setAuthenticator 的 UOE 行为。

## 1. API 功能

URLConnection 的 HTTP 专化：36 个 HTTP_* 状态码常量、setRequestMethod（7 个合法方法，非法/空/
null → ProtocolException "Invalid HTTP method: ..."；已连接 → "Can't reset method: already
connected"）、流式模式（fixed int/long：负值 IAE "invalid content length"；chunked：负/零
**不抛**、静默用默认块大小；互斥 ISE；已连接 ISE）、静态/实例重定向开关（新实例在构造时快照
静态值）、getErrorStream/getResponseCode/getResponseMessage/getPermission
（SocketPermission host:port "connect,resolve"）、抽象 disconnect/usingProxy、
JDK 9+ setAuthenticator（基类实现恒 UOE "Supplying an authenticator is not supported by ..."，
仅委托实现覆写）。

## 2. 使用方式

openConnection() 转 HttpURLConnection 后设方法/流式模式/重定向，再 connect()。本测试不发
任何网络请求：全部经最小子类与反射验证状态机。

## 3. 参考来源

- Oracle javadoc java/net/HttpURLConnection。
- OpenJDK 源 ~/git/jdk8u-dev/.../HttpURLConnection.java（常量表/方法校验/流式模式）。
- 探针 ProbeC.java E 节（双 JDK 常量值逐一比对，完全一致）。

## 4. 测试用例设计（映射到 HttpURLConnectionTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | abstract/protected（method/responseCode/responseMessage/instanceFollowRedirects/chunkLength/fixedContentLength(Long) 字段可见性）、disconnect/usingProxy abstract |
| testHttpConstants | 36 个常量精确值（1xx 无；2xx 200-206；3xx 300-305；4xx 400-415；5xx 500-505，SERVER_ERROR 与 INTERNAL_ERROR 同为 500） |
| testConstantStructure | 反射：每个 HTTP_* 字段 public static final int，恰好 36 个 |
| testRequestMethod | GET 默认、7 合法方法 round-trip、"get"/""/null → ProtocolException 精确消息（null 非 NPE！）、失败后方法不变、连接后 → ProtocolException |
| testStreamingModes | fixed(-1) int/long IAE 精确消息、0/正数/>MAX_VALUE(long) 合法、fixed→chunked ISE、chunked→fixed(int/long) ISE、fixed 重复 set 允许、chunked(<=0) 无异常、连接后 fixed/chunked ISE 精确消息 |
| testRedirects | 静态默认 true、set/get 往返+恢复、新实例构造时快照静态值、实例开关独立于后续静态变更 |
| testDefaults | getErrorStream null、getHeaderFieldKey(0)/(0) null、getHeaderFieldDate/Int 透传、getResponseCode/Message → USE("protocol doesn't support input")、getPermission SocketPermission("example.com:80"/":8080","connect,resolve")、toString 含 URL、usingProxy 委托双值 |
| testConnectDisconnect | connect/disconnect 每次调用都转发子类（计数）、disconnect 后可再 connect |
| testInheritedConnectionBehavior | 继承的 URLConnection 行为：默认值/超时/请求属性/connected 保护；子类 disconnect 清 connected 后恢复可变、可改方法 |
| testSetAuthenticator（仅 JDK-27） | 基类实现恒 UOE（含 null 参数） |
| testBlackBox（保留） | 原有黑盒冒烟 |

易错规范细节（双 JDK 实测一致）：
- setRequestMethod(null) → **ProtocolException "Invalid HTTP method: null"**，不是 NPE。
- setChunkedStreamingMode(-1)/(0) **不抛**（实现取默认块大小 4096），只有 fixed 负值抛 IAE。
- fixed(int) 后可再 fixed(long)（允许重设）；chunked 与 fixed 一旦设定互斥。
- HTTP_SERVER_ERROR == HTTP_INTERNAL_ERROR == 500（历史别名）。
- 实例 instanceFollowRedirects 在**构造时**取静态值，之后独立。

## 5. 版本差异（JDK-8 vs JDK-27，实测）

- JDK-9+ `setAuthenticator(Authenticator)`：JDK-27 侧测试 UOE（基类实现不支持非委托实现）；
  JDK-8 无此方法。
- 其余（常量值/方法矩阵/流式模式状态机/消息文本）双 JDK 完全一致。
