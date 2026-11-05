# jdk.management.jfr.EventTypeInfo 设计案

## 1. API 功能
JFR 事件类型信息：id/name/label/categoryNames/settingDescriptors。

## 2. Java 用户如何使用
FlightRecorderMXBean.getEventTypes()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/EventTypeInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒经 MXBean 获取后查询。

## 5. 深化（本轮）
断言规模：基线 21 → 31。
### 行为闭环
- FileRead（27）4 设置且 threshold 居首（8 为 3 个 enabled 居首）；全部设置值元组断言。
- equals/hashCode 未覆写：同一性语义、getEventTypes 每次新实例（fr != fr2）。
- MXBean 行通道：EventTypes 属性取 jdk.FileRead 行 → `from(row)` name/label/id/description/categoryNames/设置数全等价——27 修复了 8 侧键名错配（from 读 "settingDescriptors" 与行键一致）。
### 实测负例异常表
无专门负例（from() 通道正例）。
### 8/27 差异
8 侧 from() 读旧名 "settings" 抛 InvalidKeyException；27 正常。
