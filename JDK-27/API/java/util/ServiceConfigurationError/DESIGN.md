# java.util.ServiceConfigurationError — 测试设计案

## 1. API 功能
服务加载（ServiceLoader）配置错误时抛出的 Error 子类。构造器 String、(String, Throwable)。

## 2. Java 用户如何使用
`ServiceLoader.load(...)` 解析提供者配置失败时捕获（罕见，Error 语义）。
链路：ServiceLoader -> 配置解析失败 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `ServiceLoader`（本地 `~/git/jdk25u-dev`）解析失败抛
  `new ServiceConfigurationError(message, cause)`——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/ServiceLoader/`.

## 4. 测试用例设计 + 自动化实现（ServiceConfigurationErrorTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 继承 | extends Error |
| 构造器 | String/(String,Throwable)、消息/起因回传 |
| 捕获 | 可作为 Error 捕获 |
| P0 | 契约深化：Error 层次/cause 保持/字符串形式 | testSceContracts |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
