# java.beans.XMLEncoder 测试设计案

> 覆盖目标：完整契约（继承 Encoder + AutoCloseable、两构造器、writeObject 全类型、owner、statement/expression）。

## 1. API 功能
- 构造：`(OutputStream)`、`(OutputStream, String charset, boolean indentation, int initialSize)`。
- 写：`writeObject`、`writeStatement`、`writeExpression`、`flush`、`close`。
- owner：`setOwner`/`getOwner`。

## 2. 使用方式
- 把 bean 图写入 XML，供 `XMLDecoder` 反序列化；也可写 Statement/Expression 描述构造过程。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/XMLEncoder.html`）。
- OpenJDK `test/jdk/java/beans/XMLEncoder`（跨类 round-trip 回归）；JCK `api/java_beans/XMLEncoder`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 继承 Encoder + AutoCloseable | testStructure |
| P0 | writeObject：String/Integer/bean/数组/List/Map/null | testWriteObject |
| P1 | setOwner/getOwner | testOwner |
| P1 | 四参数构造器（charset/缩进/初始容量） | testFourArgConstructor |
| P2 | writeStatement/writeExpression | testStatements |

## 关键规范细节
- `writeObject(null)` 编码为 `<null/>`。
- bean 必须有 no-arg 构造器 + getter/setter；**不要用匿名子类**（IllegalAccessException，编码被丢弃）。
- writeStatement/writeExpression 的目标要用 bean 实例；用 String/Integer 字面量做目标会导致编码器无限递归/输出暴涨。

## 5. 深化覆盖（批次 beansE，2026-11）
新断言 95（旧 14）。追加 `testXxx` 方法（旧方法全部原样保留）：
| 测试方法 | 覆盖点 |
|---|---|
| testBeanGraphRoundTrip | 父→子嵌套 bean 图 round-trip（类名落盘、新实例、字段） |
| testCollectionsRoundTrip | List 顺序逐一断言；LinkedHashMap→Map 等价 + 逐项 |
| testArraysRoundTrip | int[]（`<array` + `length="3"`）、String[]（含空串）、boolean[] round-trip |
| testNullAndEmptyProperty | null 属性 round-trip 为 null；空串属性 round-trip |
| testEscapes | `<>&'"` 五实体（&lt;/&gt;/&amp;/&quot;/&apos;）；**不含** `&#39;`；`\r`→`&#13;`；\n/\t 字面保留；非 BMP 代理对 round-trip |
| testCharQuoting | Character `'`→`<char>&apos;</char>`、`<`→`<char>&lt;</char>`、`\u0000`→`<char code="#0"/>` |
| testDateRoundTrip | 固定毫秒 Date：`<long>1234567890123</long>` round-trip |
| testIdentityPreservation | 同一对象两次引用 → `idref` 落盘、解码后 `getA()==getB()` |
| testClassLiteral | `<class>java.lang.String</class>` round-trip |
| testHeader | `<?xml version="1.0" encoding="UTF-8"?>` + `<java version=` + `class="java.beans.XMLDecoder"`；**无 DOCTYPE**（实测） |
| testExceptionListener | 默认非空；自定义原样返回；set(null) 回退默认 |
| testFlushLifecycle | flush 前底层流 0 字节；flush 写序言+对象但** withhold `</java>`**；close 补尾；close 后 writeObject 不抛且字节不变 |
| testFlushMultiObject | 两次 flush 两对象 → 解码依序读出，第三个 AIOOBE |
| testOwnerEnvironment | setOwner(o)+writeObject(o) → XML 含 `property="owner"`；解码侧 owner 解析为**所设 owner 实例** |
| testFileOutput | 临时目录+createTempFile：文件输出与 BAOS **逐字节相等**（实测），可解码，finally 清理 |
| testCharsetVariants | US-ASCII 编 "café" → `<char code="#e9"/>` + 声明 `encoding="US-ASCII"`；UTF-16 round-trip |
| testDeclarationAndIndentation | declaration=false 无 `<?xml`；indentation=2 前缀两空格 |
| testConstructorNegatives | out=null→IAE；indentation<0→IAE；非法字符集名→IllegalCharsetNameException；未知字符集→UnsupportedCharsetException |
| testPublicFieldBean | public 字段经 `method="getField"` 落盘并 round-trip |

### 负例精确异常表（实测，JDK-8 与 JDK-27 同型）
| 触发 | 异常 |
|---|---|
| `new XMLEncoder(null)` | IllegalArgumentException |
| indentation < 0 | IllegalArgumentException |
| 非法字符集名 | java.nio.charset.IllegalCharsetNameException |
| 未知字符集名 | java.nio.charset.UnsupportedCharsetException |
| close 后 writeObject | **不抛异常**（静默挂起，无输出） |

### 易错规范细节
- 转义选择：`'` 用 `&apos;` 而非 `&#39;`；`\r` 用 `&#13;`；`\n`/`\t` 字面保留（XMLEncoder.quoteCharCode/createString）。
- 输出**没有** DOCTYPE 行（与任务书猜测相反，实测记录）。
- writeObject(null) 落盘 `<null/>`（短标签），文本不含 `>null`。
- flush 清空内部绑定：每次 flush 后可继续写新对象。
