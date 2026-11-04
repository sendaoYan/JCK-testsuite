# URLConnection 设计文档

## 一、API 功能

抽象基类，表示到 URL 的通信连接，定义请求属性、超时、缓存、内容类型猜测等通用行为，子类实现 connect()。

## 二、Java 用户如何使用

用户通过 URL.openConnection() 获取子类实例，设置 setDoInput/setDoOutput/setConnectTimeout/setReadTimeout 与请求属性后 connect() 并 getInputStream()。

## 三、开源代码实际用法调研

JDK 内建 http/file/jar 协议实现均继承 URLConnection；框架在其上封装 HTTP 客户端。

## 四、测试用例设计

用匿名子类实现 connect()；验证 connect/read 超时、doInput/doOutput/useCaches 默认与设置、请求属性 set/add/get 语义、guessContentTypeFromName。

---

## 批次 C 深化（URLConnection 簇）

> 原测试仅 ~4/40 断言的冒烟。本批补齐：最小子类默认值矩阵、超时/标志 round-trip、请求属性
> 状态机（null key/大小写/多值/connected 保护）、file: 真连接全维度（长度/类型/lastModified/
> 流同一性/内容处理器）、guessContentTypeFromName/FromStream 表、FileNameMap、
> ContentHandlerFactory 注册集成与二次安装 Error。

## 1. API 功能

抽象基类。协议处理器（file/http/jar 的 sun 内建实现）的公共超类：connect() 抽象，其余
getter 给默认值（长度 -1、类型/编码 null、时间 0、header null/空表），流访问默认抛
UnknownServiceException。管理 doInput/doOutput/useCaches/ifModifiedSince/allowUserInteraction
标志（connected 后大多数 setter 抛 IllegalStateException）、请求属性（MessageHeader，
大小写不敏感、值可 null）、connect/read 超时（负值 IAE "timeout can not be negative"，
不受 connected 保护）。静态：guessContentTypeFromName/FromStream（FileNameMap 表 + 魔数嗅探）、
get/setFileNameMap、setDefaultAllowUserInteraction、（JDK 9+）per-protocol
get/setDefaultUseCaches(String,boolean)、setContentHandlerFactory（一次 JVM 全局）。

## 2. 使用方式

URL.openConnection() 取子类实例 → 设标志/超时/请求属性 → connect()（幂等）→
getInputStream()/getContent()/getContent(Class[])。getContent 按 getContentType 经
ContentHandlerFactory（或类路径 sun.net.www.content.*）找处理器；file: 的 .txt 走
PlainTextInputStream（是 InputStream，不是 String）。

## 3. 参考来源

- Oracle javadoc java/net/URLConnection。
- OpenJDK `~/git/jdk8u-dev/jdk/src/share/classes/java/net/URLConnection.java`（本批逐条核对默认值与异常消息）。
- 探针：/tmp/probe-c/ProbeC.java（双 JDK 实测，所有断言先探后写）。

## 4. 测试用例设计（映射到 URLConnectionTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox / testPublicApiSurface | abstract/protected 成员、49 个公开方法存在性、签名（返回类型/静态/throws） |
| testMinimalDefaults | 最小子类全部默认值：-1/-1/null/null/0/0/0、header 越界（-1/0/99）、getHeaderFields 空 Map、getHeaderFieldInt/Long/Date 默认透传、getInputStream/OutputStream → USE 精确消息、getContent 同、getPermission → AllPermission、toString = 类名:url |
| testTimeouts | set(-1) IAE 精确消息、0/正数/MAX_VALUE round-trip、默认 0 |
| testFlagsAndCaches | doInput/doOutput/useCaches/allowUserInteraction 默认+设置、ifModifiedSince 负数允许、实例级 defaultUseCaches 影响新实例、静态 defaultAllowUserInteraction 保存/恢复 |
| testRequestProperties | null key set/add NPE("key is null")、getRequestProperty(null)→null、value null 允许、大小写不敏感、add 多值 getRequestProperty 返回最后值、快照 unmodifiable、含空格/换行/空串 key 均被接受（双 JDK 实测） |
| testConnectedGuard | connect 后 set/add/get/getProperties/doInput/doOutput/useCaches/ifModifiedSince/allowUserInteraction → ISE("Already connected")；setConnectTimeout/setReadTimeout 不受保护；base connect() 本身可重复调用 |
| testGuessContentTypeFromName | txt/html/htm/gif/jpg/jpeg/png/pdf/xml/zip/大小写/路径/多扩展名；noext/class/空/隐藏文件 → null；null → NPE |
| testGuessContentTypeFromStream | GIF87a/89a、JPEG SOI、PNG 签名、html 文本；未知/全零/无 mark → null；null → NPE |
| testFileConnection | file: 真连接：connect 幂等、长度 11(int/long)、text/plain、lastModified==File.lastModified()、date/expiration 0、getInputStream 两次同对象、读取内容、header content-length/type、FilePermission(path,read)、懒连接、getContent → InputStream、getContent(String[])→null、getContent(InputStream[]) 命中 |
| testDirectoryAndMissing | 目录 URL：长度>=0（平台目录尺寸）、text/plain、流可开；缺失文件：connect/getInputStream → FileNotFoundException、getContentLengthLong == 0（FileURLConnection 特例，非 -1）、getContentType 仍按名猜测不触连接 |
| testFileNameMap | 默认表 .txt/.html、setFileNameMap 自定义后 guess 生效、恢复 |
| testDefaultRequestPropertyDeprecated | 废弃 API 为 no-op，get 恒 null |
| testExtraSemantics | set 覆盖单值、guess 后流位置保持（mark/reset）、getInputStream 不触发 connect |
| testContentHandlerFactory（最后跑） | 注册后 getContent 走工厂（哨兵）、mime=text/html；二次安装 → Error("factory already defined") |

易错规范细节（探针实测，双 JDK 一致）：
- getHeaderFields() 默认返回**空 Map**（非 null）。
- setRequestProperty("K", null) **不抛**，get 返回 null；只有 key 为 null 抛 NPE。
- 请求属性 key 大小写不敏感（MessageHeader）。
- addRequestProperty 多值时 getRequestProperty 返回**最后添加**的值。
- getPermission 基类返回 AllPermission（SecurityConstants.ALL_PERMISSION）；HttpURLConnection 覆写为 SocketPermission。
- 缺失 file: 的 getContentLengthLong 是 0（FileURLConnection 捕获 FNFE 后返回 0），真实 -1 出现在缺失 jar entry。
- getContentHandler 按 mime 缓存处理器：工厂集成测试必须用未缓存过的 mime（.html），否则静态缓存遮蔽工厂。

## 5. 版本差异（JDK-8 vs JDK-27，实测）

- `URLConnection.guessContentTypeFromName("x.css")`：JDK-8 → null（表无映射），JDK-27 → "text/css"。
- file: 连接的 `getHeaderFields()`：JDK-8 空 Map；JDK-27 含 content-length/last-modified/content-type 三项。
- `getFileNameMap()`：JDK-8 每次调用返回新包装（无身份稳定性，只能功能性断言）；JDK-27 身份稳定。
- `getRequestProperties()` 多值列表顺序：JDK-8 [b,a]（后加在前），JDK-27 [a,b]（插入序）——只断言包含关系。
- NPE 消息文本：JDK-27 带 helpful message（"Cannot invoke ..."），JDK-8 为 null —— NPE 一律只断言类型。
- JDK-9+ 独有：URLConnection.setDefaultUseCaches(String,boolean)/getDefaultUseCaches(String)、
  HttpURLConnection.setAuthenticator（基类恒 UOE）。JDK-8 侧对应测试方法删除。
