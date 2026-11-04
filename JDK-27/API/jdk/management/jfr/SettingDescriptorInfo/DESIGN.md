# jdk.management.jfr.SettingDescriptorInfo 设计案

## 1. API 功能
JFR 设置描述符信息：name/label/typeName/contentType。

## 2. Java 用户如何使用
EventTypeInfo.getSettingDescriptors()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.management.jfr/share/classes/jdk/management/jfr/SettingDescriptorInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒经 MXBean 获取后查询。
