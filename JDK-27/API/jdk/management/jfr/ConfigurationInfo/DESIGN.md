# jdk.management.jfr.ConfigurationInfo 设计案

## 1. API 功能
JFR 配置信息（final class）：name/label/provider/contents/settings。

## 2. Java 用户如何使用
FlightRecorderMXBean.getConfigurations()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/ConfigurationInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒经 MXBean 获取后查询。
