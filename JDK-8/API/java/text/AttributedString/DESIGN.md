# java.text.AttributedString 测试设计案（JDK-8）

> 覆盖目标：完整契约（五个构造器、addAttribute/addAttributes 全量/区间、三种 getIterator、
> 复制构造器、错误路径、空串），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`AttributedString(String)`、`(String, Map)`、`(AttributedCharacterIterator)`、
  `(AttributedCharacterIterator, int, int)`、`(AttributedCharacterIterator, int, int, Attribute[])`。
- 属性：`addAttribute(Attribute, Object)`、`addAttribute(Attribute, Object, int, int)`、
  `addAttributes(Map, int, int)`。
- 迭代：`getIterator()`、`getIterator(Attribute[])`、`getIterator(Attribute[], int, int)`。

## 2. Java 用户如何使用

- `new AttributedString("hello")` → `addAttribute(LANGUAGE, "en", 1, 3)` → `getIterator()` 遍历
  并 `getAttribute` 取回属性。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/AttributedString.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 AttributedStringTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：五个构造器 + addAttribute/addAttributes/getIterator 签名 | testWhiteBox |
| P0 | String 构造、Map 构造、addAttribute 全量/区间 | testBlackBox |
| P0 | addAttributes(Map, begin, end) | testBlackBox |
| P0 | getIterator(Attribute[])/getIterator(Attribute[], begin, end) | testBlackBox |
| P0 | ACI 复制构造器（全量/区间归一化） | testBlackBox |
| P0 | 错误路径（null 文本/越界区间/null 属性） + 空串 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `addAttribute` 区间非法（start>end/负/越界）抛 `IllegalArgumentException`；null 属性/文本抛 NPE。

## 注意事项

- 无 Locale/墙钟/随机；headless-safe 不加 `@key`。
