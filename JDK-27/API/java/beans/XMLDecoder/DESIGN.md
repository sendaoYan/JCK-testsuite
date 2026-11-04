# java.beans.XMLDecoder 测试设计案

> 覆盖目标：完整契约（AutoCloseable、五构造器、bean/数组/List/Map/String/Integer/null round-trip、owner、异常监听器、坏输入）。

## 1. API 功能
- 构造：`(InputStream)`、`(InputStream, owner)`、`(InputStream, owner, ExceptionListener)`、`(InputStream, owner, ExceptionListener, ClassLoader)`、`(InputSource)`。
- 读：`readObject`；owner：`setOwner`/`getOwner`；异常监听器：`setExceptionListener`/`getExceptionListener`；`close`。
- 静态：`createHandler(owner, listener, cl)` 返回 SAX `DefaultHandler`。

## 2. 使用方式
- 读取 `XMLEncoder` 产生的 XML 还原对象图。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/XMLDecoder.html`）。
- OpenJDK `test/jdk/java/beans/XMLDecoder`；JCK `api/java_beans/XMLDecoder`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | AutoCloseable 结构 | testStructure |
| P0 | bean/数组/List/Map/String/Integer/null round-trip | testRoundTrip |
| P0 | 五构造器 + setOwner/setExceptionListener + createHandler | testConstructorsAndOwner |
| P1 | 空/垃圾输入 readObject 抛 ArrayIndexOutOfBoundsException | testBadInput |

## 关键规范细节
- 空输入或非法 XML 在 `readObject` 时抛 `ArrayIndexOutOfBoundsException`（JDK8 消息为 "0"，JDK27 为 "Index 0 out of bounds for length 0"——只断言类型不断言消息）。
- 默认 ExceptionListener 会把编码异常打到 stderr 并继续；可注入自定义监听器。

## 5. 深化覆盖（批次 beansE，2026-11）
新断言 71（旧 20）。追加 `testXxx` 方法（旧方法全部原样保留）：
| 测试方法 | 覆盖点 |
|---|---|
| testLiteralPrimitives | 手写 XML 字面量：string/int/boolean/long/short/byte/char/float/double/空串/空格保留/`<null/>` |
| testLiteralCollections | `<object class="java.util.ArrayList">` + `void method="add"` → [a,b] 顺序 |
| testLiteralArray | `<array class length>` + `void index` → 定长数组逐元素 |
| testLiteralClassAndMultipleRoots | `<class>` 解码 String.class；多根文档依序读出、末尾 AIOOBE |
| testTruncatedXmlCapturedByListener | 截断 XML → listener 收 SAXParseException、readObject 抛 AIOOBE、close 正常 |
| testUnknownClassCapturedByListener | `no.such.Clazz` → listener 首个 ClassNotFoundException、readObject AIOOBE |
| testFaultTolerantContinuation | 坏元素后的 `<int>5</int>` 仍解出（容错续读） |
| testCloseThenRead | close 后 readObject 仍返回对象；close 幂等 |
| testNullInputStream | `XMLDecoder((InputStream)null)` 构造 OK；readObject AIOOBE；close 不抛 |
| testTwoDecodersIndependent | 同字节两解码器 → equal 且不同实例 |
| testInputSourceVariants | InputSource(StringReader)/InputSource(InputStream)；默认 owner null |
| testListenerDefaults | 默认 listener 非空；setExceptionListener(null) 回退默认；setOwner/getOwner |
| testOwnerEnvironment | owner 引用档案解码为所设 owner 实例（== 同一性） |
| testFlushedArchive | encoder 两次 flush 的档案依序读出两个对象，第三个 AIOOBE |
| testRoundTrip(扩展) | long[]/Double 追加 round-trip |

### 负例精确异常表（实测，双侧同型；消息文本不同不断言）
| 触发 | 异常 |
|---|---|
| 空输入/垃圾输入/读到末尾 readObject | ArrayIndexOutOfBoundsException |
| 截断 XML 的 readObject | ArrayIndexOutOfBoundsException（SAXParseException 进 listener） |
| 未知类 readObject | ArrayIndexOutOfBoundsException（ClassNotFoundException 进 listener） |
| null 输入流 readObject | ArrayIndexOutOfBoundsException（默认 listener 打印 MalformedURLException 后继续） |

### 易错规范细节
- `<array>` 字面量必须用 `<void index="N">` 子元素；直接嵌 `<string>` 会报 "Could not add argument to evaluated element"（IllegalStateException 进默认监听器）。
- close() 不失效化解码器：已 close 后 readObject 仍可读（数组已缓存）；对截断档案 close 会再次驱动解析（若 listener 抛异常会从 close 穿出）。
- 显式 BeanInfo 的兄弟类查找规则见 DPD 设计案（嵌套在 bean 内无效）。

### 版本差异（实测）
- 公开 API 与行为双侧一致；AIOOBE 消息文本不同（"0" vs "Index 0 out of bounds for length 0"），只断言类型。
