# jdk.management.jfr.SettingDescriptorInfo 设计案

## 1. API 功能
JFR 设置描述符信息：name/label/typeName/contentType。

## 2. Java 用户如何使用
EventTypeInfo.getSettingDescriptors()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/SettingDescriptorInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒经 MXBean 获取后查询。

## 5. 深化（本轮）
断言规模：基线 21 → 33。
### 行为闭环
- threshold 全值元组：label "Threshold"、default "0 ns"、typeName jdk.settings.Threshold、contentType jdk.jfr.Timespan；enabled 全值元组同构。
- equals/hashCode 未覆写（同一性）。
- 双通道：EventTypes 行内嵌套键 `settingDescriptors`（27 实测键名，长度 4）取 threshold 行 → `from()` 6 字段全等价。
### 实测负例异常表
无专门负例（from() 通道正例）。
### 8/27 差异
8 嵌套长度 3、顺序 [enabled, threshold, stackTrace]；27 长度 4、threshold 居首。
