# java.util.MissingResourceException — 测试设计案

## 1. API 功能
资源束缺失时抛出的运行时异常。构造器 (String message, String className, String key)，getClassName()/getKey()。

## 2. Java 用户如何使用
`ResourceBundle.getBundle(...)` 找不到对应 bundle 时抛出；用户用 getClassName()/getKey() 定位缺失资源。
链路：getBundle -> 查找失败 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `ResourceBundle`（本地 `~/git/jdk25u-dev`）查找失败抛
  `new MissingResourceException(msg, className, key)`——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/ResourceBundle/`.

## 4. 测试用例设计 + 自动化实现（MissingResourceExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 构造+getter | getClassName/getKey/getMessage 精确值 |
| 触发 | getBundle(不存在) 抛 MissingResourceException |
| 继承 | extends RuntimeException |
| P0 | 契约深化：层次/构造器矩阵/getter 保持 | testMissingResourceContracts |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
