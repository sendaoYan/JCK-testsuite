# java.util.Enumeration — 测试设计案

## 1. API 功能
遗留的只读游标接口（Iterator 的前身）：hasMoreElements/nextElement，无 remove。

- 抽象：`hasMoreElements()`、`nextElement()`。
- 桥接：`asIterator()`（default）转成 Iterator。

## 2. Java 用户如何使用
`Collections.enumeration(c)` / `Vector.elements()` 得到枚举，`while (e.hasMoreElements())
x = e.nextElement()` 遍历；`asIterator()` 桥接到增强 for/forEachRemaining。
链路：枚举 -> hasMoreElements/nextElement 耗尽 -> nextElement 抛 NSEE -> asIterator 桥接。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Enumeration.java`（本地 `~/git/jdk25u-dev`）：
  `asIterator()` 是 default 方法，内部匿名 Iterator 委托枚举——第 4 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/Collections.java#enumeration` 与
  `Vector.elements()` 是主要生产源。
- 遗留 servlet API（`javax.servlet`）大量使用 Enumeration（HttpServletRequest.getAttributeNames）。

## 4. 测试用例设计 + 自动化实现（EnumerationTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 遍历 | hasMoreElements/nextElement 逐元素、耗尽 |
| 异常 | 耗尽后 nextElement 抛 NoSuchElementException |
| 生产源 | Vector.elements、空枚举 |
| asIterator | 桥接 forEachRemaining、remove 抛 UnsupportedOperationException |

共 4 组场景、约 10 个 check（`testBlackBox()`）。
