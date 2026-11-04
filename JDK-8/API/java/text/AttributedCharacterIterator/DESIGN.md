# java.text.AttributedCharacterIterator 测试设计案（JDK-8）

> 覆盖目标：完整契约（接口纯抽象、extends CharacterIterator、run 边界、属性 get/set、
> getAllAttributeKeys、无属性文本、clone），非冒烟。

## 1. API 提供了哪些功能

- 接口：extends `CharacterIterator`。
- run 边界：`getRunStart()/getRunLimit()` 及按 `Attribute`、`Set<Attribute>` 的版本。
- 属性：`getAttributes()`、`getAttribute(Attribute)`、`getAllAttributeKeys()`。

## 2. Java 用户如何使用

- `new AttributedString("...").getIterator()` → `getRunStart/getRunLimit` 定位 run →
  `getAttribute(LANGUAGE)` 取属性值 → `getAllAttributeKeys` 枚举全部属性。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/AttributedCharacterIterator.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 AttributedCharacterIteratorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：接口、extends CharacterIterator、方法全抽象 | testWhiteBox |
| P0 | 组合 run 边界（跨属性） | testBlackBox |
| P0 | 单属性 run 边界 | testBlackBox |
| P0 | Set 属性 run 边界 | testBlackBox |
| P0 | getAttributes/getAttribute 取值 | testBlackBox |
| P0 | getAllAttributeKeys | testBlackBox |
| P0 | 无属性文本 + clone | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- 组合 run 边界由所有属性共同决定；单属性 run 边界只由该属性决定。
- 无属性文本 `getRunStart(LANG)==begin`、`getAttributes().isEmpty()`。

## 注意事项

- 无 Locale/墙钟/随机；headless-safe 不加 `@key`。
