# java.text.Annotation 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/getValue/toString、身份语义、AttributedString 真实用法、嵌套），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`Annotation(Object value)`（值可为 null）。
- 查询：`getValue()`；`toString()`（`java.text.Annotation[value=...]`）。
- 无 equals/hashCode 覆写（身份语义）、不 Serializable。

## 2. Java 用户如何使用

- 作为 `AttributedString.addAttribute(READING, new Annotation(obj), start, end)` 的属性值，
  从 `AttributedCharacterIterator.getAttribute` 取回后 `getValue()`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/Annotation.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 AnnotationTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：具体非 final、单公开构造器、getValue/toString、非 Serializable | testWhiteBox |
| P0 | 多种 payload（String/Integer/Object 同一性/null）往返 | testBlackBox |
| P0 | 身份语义（无 equals 覆写） | testBlackBox |
| P0 | toString 精确格式 | testBlackBox |
| P0 | AttributedString 属性值存取 Annotation（run 内/外、null payload） | testBlackBox |
| P0 | 嵌套 Annotation | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- 无 equals 覆写：同值两个 Annotation 互不相等。
- `toString()` 为 `java.text.Annotation[value=...]`，null 渲染为 `value=null`。

## 注意事项

- 纯对象构造，无 Locale/墙钟/全局副作用；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 身份语义（equals 不覆写、identity hashCode、toString `java.text.Annotation[value=s]` 精确）
- null 值合法可读；任意 Object 值往返
- 作为 AttributedString 属性值无损传递
