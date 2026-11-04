# java.util.IllegalFormatException — 测试设计案

## 1. API 功能
所有 `Formatter` 格式字符串非法时抛出异常的抽象基类。继承 IllegalArgumentException。不直接实例化。

## 2. Java 用户如何使用
用户 `catch (IllegalFormatException e)` 统一捕获格式化错误；具体子类提供 flags/conversion/precision 等
上下文信息。链路：Formatter.format -> 语法/参数校验 -> 抛具体子类（皆为 IllegalFormatException 实例）。

## 3. 开源代码实际用法调研
- OpenJDK `java.util.IllegalFormatException`（本地 `~/git/jdk25u-dev`）为抽象类 extends IllegalArgumentException；
  `Formatter` 中 13 个具体子类均继承它——第 1/2 节对齐。
- 官方测试 `test/jdk/java/util/Formatter/`.

## 4. 测试用例设计 + 自动化实现（IllegalFormatExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 抽象性 | abstract、extends IllegalArgumentException |
| 子类关系 | 具体子类均 instanceof IllegalFormatException |
| 捕获 | 可作为 IllegalArgumentException 捕获 |

共 3 组场景、约 15 个 check（`testBlackBox()`）。
