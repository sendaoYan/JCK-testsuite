# jdk.jfr.SettingDescriptor 设计案

## 1. API 功能
设置描述符（final）：getName/getTypeName/getDefaultValue/getLabel（经 EventType 获取）。

## 2. Java 用户如何使用
EventType.getSettingDescriptors() 遍历读取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/SettingDescriptor.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/关键方法；黑盒 自定义事件类型 settings 非空 + enabled 存在。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 内建三件表（自定义事件上）：enabled{默认"true", contentType jdk.jfr.BooleanFlag, label/description 非空, typeId>0}；threshold{默认"0 ns", contentType jdk.jfr.Timespan, label "Threshold"}；stackTrace{默认"true", contentType jdk.jfr.BooleanFlag}。
- 自定义控件描述符：默认值=控件 getValue()；typeName=控件类名；label/description/contentType 均 null；annotationElements 空；getAnnotation(Label/Timespan) null（setting 派生注解不走 getAnnotation）。
- 全部描述符 name 非空、typeName 非空、typeId&gt;0。
负例：无（纯读 API）。
