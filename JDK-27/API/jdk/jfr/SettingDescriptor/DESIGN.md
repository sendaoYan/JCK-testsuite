# jdk.jfr.SettingDescriptor 设计案

## 1. API 功能
设置描述符（final）：getName/getTypeName/getDefaultValue/getLabel（经 EventType 获取）。

## 2. Java 用户如何使用
EventType.getSettingDescriptors() 遍历读取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/SettingDescriptor.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/关键方法；黑盒 自定义事件类型 settings 非空 + enabled 存在。
