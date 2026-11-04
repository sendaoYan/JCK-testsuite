# JarURLConnection 设计文档

## 一、API 功能

抽象类，表示到 jar 文件或 jar 内条目的 URL 连接，可访问 jar 文件、清单(Manifest)、条目与证书。

## 二、Java 用户如何使用

用户把 jar:...!/... URL 的 openConnection() 结果强转为 JarURLConnection，调用 getJarFile()/getManifest()/getJarEntry()/getAttributes()/getCertificates()。

## 三、开源代码实际用法调研

应用服务器、类加载器、构建工具读取 jar 内资源与元数据时使用。

## 四、测试用例设计

子类暴露受保护构造器验证 jarFileURL/entryName 解析(含百分号解码与无 !/ 抛 MalformedURLException)；用临时 jar 验证 getJarFile/getManifest/getJarEntry/getMainAttributes/getAttributes/getCertificates/getInputStream。

---

## 批次 C 深化（URLConnection 簇）

> 原测试 ~17 断言（JDK-27）/ 4（JDK-8）。本批补齐：现场造 jar（主属性+逐 entry 属性）后
> whole/entry 两类 URL 全维度、缺失 entry 行为、抽象子类 null 委托 NPE 矩阵、规格解析
> （百分号解码、无 "!/"、内嵌非 file URL）。

## 1. API 功能

jar: URL 连接。构造时解析 spec：`jar:<jarURL>!/[entryName]`，entryName 百分号解码，缺 "!/"
→ MalformedURLException("no !/ in spec")。getJarFile/getJarEntry/getManifest/getAttributes/
getMainAttributes/getCertificates 全部委托 getJarFile()：whole URL getJarEntry/getAttributes/
getCertificates 为 null、getManifest 为 jar 清单；entry URL 的 getAttributes 取清单中该
entry 的节（没有则 null）。whole URL getContentLengthLong == jar 文件尺寸、getContentType
"x-java/jar"、getContent() 返回 JarFile；entry URL 长度 == entry 尺寸、getInputStream 读
entry 内容（whole URL getInputStream → IOException "no entry name specified"）。缺失 entry：
getInputStream → FileNotFoundException、getContentLengthLong == -1。

## 2. 使用方式

new URL("jar:file:/path/x.jar!/entry").openConnection() 强转 JarURLConnection；getJarFile()
结果会被缓存（同一连接两次调用同实例），用完 close。

## 3. 参考来源

- Oracle javadoc；OpenJDK JarURLConnection.java。
- 探针 ProbeC.java F/F2 节（双 JDK 比对：仅缺失 entry 的 FNFE 消息文本与 NPE 消息文本不同）。

## 4. 测试用例设计（映射到 JarURLConnectionTest.java）

| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | protected ctor/jarFileURLConnection 字段、getJarFile abstract、其余方法存在 |
| testSpecParsing | whole/entry/百分号编码/目录 entry 名解析、jarFileURL、无 "!/" → MUE、内嵌 http URL 允许 |
| testWholeJar | connect 幂等、getJarFile 缓存同实例、manifest 主属性、entryName null、getJarEntry/Attributes/Certificates null、长度==文件尺寸、x-java/jar、lastModified>0、FilePermission read、getContent → JarFile、getInputStream → IOException("no entry name specified") |
| testEntry | entryName/jarEntry 名与尺寸(11)/长度 int+long、清单逐 entry 属性(Custom-Attr)、getMainAttributes 恒为清单主节、certificates null、getInputStream 读出全文、第二次新流、子目录 entry |
| testMissingEntry | getInputStream → FileNotFoundException（消息文本版本差异，仅断言类型）、getContentLengthLong/-1 int |
| testNullDelegationDefaults | getJarFile()==null 时 getManifest/getMainAttributes → NPE；getJarEntry 覆写为 null 时 getAttributes/getCertificates → null 不 NPE；getContentLength(int/long) -1 |
| testBlackBox（保留） | 原有真 jar 冒烟 + readAllBytes(JDK-8 侧换手写读) |

易错规范细节：
- getAttributes/getCertificates 的 null 语义取决于 getJarEntry 是否返回 null，而非清单内容。
- whole-jar getInputStream 抛 IOException("no entry name specified")（双 JDK 一致）。
- 缺失 entry：JDK-8 消息 "JAR entry nope.txt not found in <path>"，JDK-27 "JAR entry not found in jar file" —— 只断言类型。

## 5. 版本差异（JDK-8 vs JDK-27，实测）

- 缺失 entry 的 FileNotFoundException 消息：JDK-8 "JAR entry X not found in <path>"，
  JDK-27 "JAR entry not found in jar file" —— 仅断言类型。
- getJarFile()==null 委托 NPE 的消息文本不同（JDK-27 helpful message）—— 仅断言类型。
