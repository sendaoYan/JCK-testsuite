# jdk.management.jfr.ConfigurationInfo 设计案（JDK-8）

## 1. API 功能
预定义配置值对象：name/label/description/provider/contents/settings + static `from(CompositeData)`。

## 2. Java 用户如何使用
`FlightRecorderMXBean.getConfigurations()` 的元素；setPredefinedConfiguration 可用名目录。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.management.jfr/share/classes/jdk/management/jfr/ConfigurationInfo.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（default/profile 全 getter + MXBean 行 → from() 等价）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 8 → 33。
### 行为闭环
- default：label "Continuous"、provider "Oracle"、description/contents（jfc XML，以 "<" 开头）非空、settings 236 条且键为 `event#setting` 形式。
- profile：label "Profiling"（实测，非 "Profile"）、settings 与 default 不同。
- MXBean 行形状 6 项 [contents, description, label, name, provider, settings]；`from(行)` 全字段等价（含 settings map 完全往返——ConfigurationInfo 的 from 是 8 侧三个值对象中唯一完全可用的）。
### 实测负例异常表
无专门负例（from() 通道正例）。
### 8/27 差异
27 侧 default label 同为 Continuous；profile label 27 侧为 "Profile"（8 为 "Profiling"）——名称演进差异。
