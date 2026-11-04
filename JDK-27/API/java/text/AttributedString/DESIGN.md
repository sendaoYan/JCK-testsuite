# java.text.AttributedString — 测试设计案

## 1. API 功能
带属性文本的**构建器**：把纯文本与若干 `(Attribute, value, [begin, end))` 标注组合成可迭代的
`AttributedCharacterIterator`。

- 构造：`AttributedString(String)`；`AttributedString(String, Map)`（整串应用一组属性）；
  `AttributedString(AttributedCharacterIterator)`；`(…, int begin, int end)`；
  `(…, int begin, int end, Attribute[] keys)`（拷贝迭代器的一段，可过滤属性键）。
- 标注：`addAttribute(Attribute, Object)`（整串）、`addAttribute(Attribute, Object, int, int)`（区间）、
  `addAttributes(Map, int, int)`（区间批量）。
- 输出：`getIterator()`、`getIterator(Attribute[])`、`getIterator(Attribute[], int, int)`。

## 2. Java 用户如何使用
`AttributedString as = new AttributedString("hello"); as.addAttribute(LANGUAGE, "en", 0, 5);
AttributedCharacterIterator it = as.getIterator();` 多段叠加标注后统一迭代；从已有 iterator
拷贝时用 `new AttributedString(otherIt)` 保留文本+属性，或用 range/keys 重载裁剪。
链路：new AttributedString -> addAttribute(区间) -> getIterator -> getAttribute/run 查询。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/AttributedString.java`（本地 `~/git/jdk25u-dev`）：
  `getIterator()` 即 `getIterator(null)`，`getIterator(attrs)` 返回内部 `AttributedStringIterator`
  且构造时校验 `beginIndex<0 || beginIndex>endIndex || endIndex>length()` 抛
  `IllegalArgumentException("Invalid substring range")`；`addAttribute` 校验 start/end 越界——
  第 8 节断言对齐。
- OpenJDK `test/jdk/java/text/AttributedString/AttributedStringTest.java` 与
  `TestAttributedStringCtor.java`：官方测试覆盖各构造与 range 拷贝。
- OpenJDK `src/java.desktop/share/classes/java/awt/font/TextLayout.java`：桌面文本排版消费
  AttributedString 构造带属性文本，是典型下游。
- Oracle javadoc
  (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/AttributedString.html)。

## 4. 测试用例设计 + 自动化实现（AttributedStringTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| String 构造 | 迭代器 span 全文、无属性、可游标读出文本 |
| Map 构造 | 属性整串生效、单一 run [0,len) |
| addAttribute 整串/区间 | 整串生效；区间 [1,3) 内生效、[3,4) 外 null、run 边界正确 |
| addAttributes(Map,range) | [2,5) 生效、范围外 null、run 边界正确 |
| getIterator(Attribute[]) | 返回可遍历整串的迭代器（文本与 bounds 正确）；子窗口重载 getIterator(attrs,begin,end) 正确裁剪 begin/end 并保留属性 |
| 迭代器拷贝构造 | 保留属性与文本长度；range 拷贝归一化索引并保留重叠属性 |
| 错误路径 | null 文本/空字符串、非法区间（start>end/负/越界）、null 属性键 |

共 8 组场景、约 27 个 check（`testBlackBox()`）。

## 契约深化（本批新增，两侧同构断言）

- 构造矩阵（null String/Map→NPE、Map ctor 全属性全文应用、迭代器拷贝构造保留属性）
- addAttribute 范围矩阵（start>end/end>length/空范围→IAE、null value 存 null、null attr→NPE）
- getIterator(null=全部属性、范围越界 IAE、start>end IAE)
- 文本重建 + 相邻同属性范围恰在边界分裂
