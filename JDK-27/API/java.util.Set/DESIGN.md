# java.util.Set — 测试设计案

## 1. API 功能
不重复元素集合基接口。继承 Collection。JDK-27 预览新增 `ofLazy`。

- static：of/copyOf（以及预览 ofLazy）。

## 2. Java 用户如何使用
`Set.of("a","b")` 不可变工厂、`copyOf` 拷贝、`ofLazy(keys, pred)` 惰性过滤。
链路：of -> 重复元素 IAE -> copyOf -> ofLazy。

## 3. 开源代码实际用法调研
- OpenJDK `Set`（本地 `~/git/jdk25u-dev`）：of 拒绝重复元素（IAE）与 null（NPE）——
  第 1/2/4 节对齐；JDK-27 ofLazy 需 `--enable-preview`。
- 官方测试 `test/jdk/java/util/Set/`.

## 4. 测试用例设计 + 自动化实现（SetTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| of 工厂 | 空/多元素、重复 IAE、null NPE |
| copyOf | 拷贝内容 |
| ofLazy | 惰性过滤 |

共 3 组场景、约 7 个 check（`testBlackBox()`）。
