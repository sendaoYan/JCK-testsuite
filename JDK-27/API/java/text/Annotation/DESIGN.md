# java.text.Annotation — 测试设计案

## 1. API 功能
`Annotation` 是属性文本中的**轻量值包装**：当一个属性值只在一段 run 内有意义（不随文本被
复制/拼接而延续）时，用 Annotation 包住它（典型：READING、INPUT_METHOD_SEGMENT 的值）。

- 构造：`Annotation(Object value)`（值可为 null）。
- 查询：`Object getValue()`。
- 其他：`toString()`（`java.text.Annotation[value=...]`）。无 equals/hashCode 覆写（身份语义），
  不实现 Serializable。

## 2. Java 用户如何使用
输入法/富文本程序把 Annotation 作为 `AttributedString.addAttribute(attr, new Annotation(obj),
start, end)` 的值：`as.addAttribute(READING, new Annotation("pronunciation"), 0, 4)`；随后从
`AttributedCharacterIterator.getAttribute(READING)` 取回 Annotation 并 `getValue()`。
链路：new Annotation(payload) -> addAttribute 存入 run -> iterate -> getAttribute -> getValue
往返；嵌套 Annotation（值又是 Annotation）表示分层标注。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/Annotation.java`（本地 `~/git/jdk25u-dev`）：
  类注释 “Annotation objects are used as attribute values... If the attributes are not
  continued, the value is wrapped in an Annotation instance”；toString 为
  `getClass().getName() + "[value=" + value + "]"`——第 3 节断言与实现一致。
- OpenJDK `src/java.base/share/classes/java/text/AttributedString.java`：javadoc 明确
  “attribute values... such as Annotation instances” 且构造 AttributedString(iterator) 时
  Annotation 值到 run 边界即截断。
- OpenJDK `src/java.desktop/share/classes/java/awt/font/TextAttribute.java`（用法权威）：
  TextAttribute.READING 的文档要求 “values are instances of Annotation holding String”。
- Oracle javadoc
  (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/Annotation.html)：
  “Annotation object is used as a wrapper for attribute values that are carried with the text
  but not merged across runs”。

## 4. 测试用例设计 + 自动化实现（AnnotationTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 值往返 | String/Integer/任意 Object（同一性）/null 四种 payload 的 getValue |
| 身份语义 | 无 equals 覆写：两个同值实例互不相等，自反成立 |
| toString | 精确 `"java.text.Annotation[value=segment1]"`、数字与 null 形态 |
| 真实用法 | AttributedString 中 READING/INPUT_METHOD_SEGMENT 属性值存取 Annotation：
  run 内取回并 getValue；run 外为 null；null payload 的 Annotation 可取回 |
| 嵌套 | Annotation(Annotation("inner")) 双层 getValue |

共 5 组场景、约 16 个 check（`testBlackBox()`）。
