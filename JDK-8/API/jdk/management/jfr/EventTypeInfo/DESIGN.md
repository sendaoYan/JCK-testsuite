# jdk.management.jfr.EventTypeInfo 设计案（JDK-8）

## 1. API 功能
事件类型值对象：id/name/label/description/categoryNames/settingDescriptors + static `from(CompositeData)`。

## 2. Java 用户如何使用
`FlightRecorderMXBean.getEventTypes()` 的元素；远程通道重建事件类型目录。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.management.jfr/share/classes/jdk/management/jfr/EventTypeInfo.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（FileRead 全值元组 + MXBean 行形状 + from() 负例）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 8 → 30。
### 行为闭环
- 注册表 >100 条（实测 129）；jdk.FileRead 在列且 categorized under "Java Application"；equals/hashCode 未覆写（同一性语义、getEventTypes 每次新实例）。
- FileRead 3 设置全值元组：enabled/threshold/stackTrace 各自 label/default/typeName/contentType 逐项断言。
- MXBean 行形状：6 项 [categoryNames, description, id, label, name, settingDescriptors]，id LONG，嵌套 settingDescriptors 长度 3。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `EventTypeInfo.from(自家 MXBean 的行)` | javax.management.openmbean.InvalidKeyException | `Argument key="settings" is not an existing item name for this CompositeData instance.`（8 的 from() 读旧名 "settings"，行键是 "settingDescriptors"——backport 缺陷，稳定复现） |
### 8/27 差异
27 侧 from() 读 "settingDescriptors" 与行键一致，往返等价成立；8 侧 from() 只对含 "settings" 键的手工形状可用。
