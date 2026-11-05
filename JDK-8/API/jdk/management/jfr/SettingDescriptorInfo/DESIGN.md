# jdk.management.jfr.SettingDescriptorInfo 设计案（JDK-8）

## 1. API 功能
设置描述值对象：name/label/description/typeName/contentType/defaultValue + static `from(CompositeData)`。

## 2. Java 用户如何使用
EventTypeInfo.getSettingDescriptors() 的元素；远程通道重建设置目录。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.management.jfr/share/classes/jdk/management/jfr/SettingDescriptorInfo.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（FileRead 三设置全值 + 嵌套行 → from() 等价）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 8 → 38。
### 行为闭环
- FileRead 三设置全值元组：enabled（"Enabled"/"false"/jdk.settings.Enabled/jdk.jfr.BooleanFlag）、threshold（"Threshold"/"0 ns"/jdk.settings.Threshold/jdk.jfr.Timespan）、stackTrace（"Stack Trace"/"false"/jdk.settings.StackTrace/jdk.jfr.BooleanFlag）。
- toString 实测形状 `name="enabled" ... defaultValue="false"`（含引号包裹的键值）。
- equals/hashCode 未覆写（同一性）。
- MXBean 嵌套行：EventTypes 行内 settingDescriptors 数组长度 3、行形状 6 项 [contentType, defaultValue, description, label, name, typeName]；`from(嵌套行)` 全 6 字段等价（可用通道）。
### 实测负例异常表
无专门负例（from() 通道正例）。
### 8/27 差异
8 FileRead 3 设置无 throttle；27 4 设置（threshold/enabled/stackTrace/throttle）且顺序不同。
