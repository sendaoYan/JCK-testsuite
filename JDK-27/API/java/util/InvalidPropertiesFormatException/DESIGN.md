# java.util.InvalidPropertiesFormatException — 测试设计案

## 1. API 功能
`Properties.loadFromXML/storeToXML` 处理非法 XML 时抛出的受检异常（IOException 子类）。构造器 (Throwable)、String。

## 2. Java 用户如何使用
加载损坏的 properties XML 时捕获；getCause() 拿到底层 XML 解析异常。
链路：loadFromXML -> XML 解析失败 -> 抛出。

## 3. 开源代码实际用法调研
- OpenJDK `Properties.loadFromXML`（本地 `~/git/jdk25u-dev`）解析失败抛
  `new InvalidPropertiesFormatException(e)`——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/Properties/`.

## 4. 测试用例设计 + 自动化实现（InvalidPropertiesFormatExceptionTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 继承 | extends IOException |
| 构造器 | String/Throwable 构造、消息/起因回传 |
| 触发 | loadFromXML(坏 XML) 抛 InvalidPropertiesFormatException |
| P0 | 契约深化：两种构造器语义/cause 无包装 | testIpfContracts |

共 3 组场景、约 5 个 check（`testBlackBox()`）。
