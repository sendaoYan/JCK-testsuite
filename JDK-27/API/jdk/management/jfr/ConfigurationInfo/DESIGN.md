# jdk.management.jfr.ConfigurationInfo 设计案

## 1. API 功能
JFR 配置信息（final class）：name/label/provider/contents/settings。

## 2. Java 用户如何使用
FlightRecorderMXBean.getConfigurations()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/ConfigurationInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒经 MXBean 获取后查询。

## 5. 深化（本轮）
断言规模：基线 20 → 28。
### 行为闭环
- default/profile 全 getter：label（default=Continuous）、provider、description、contents（XML）、settings（键含 "#"）。
- MXBean 行通道：Configurations 属性取 default 行 → `from(row)` 全字段等价（含 settings map 完全往返——双侧唯一完全可用的 from）。
### 实测负例异常表
无专门负例（from() 通道正例）。
### 8/27 差异
27 profile label 为 "Profile"（8 为 "Profiling"）。
