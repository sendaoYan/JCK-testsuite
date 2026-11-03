# java.beans.PropertyEditorManager — 设计案

## 1. API 功能
属性编辑器注册表。registerEditor/findEditor、get/setEditorSearchPath。

## 2. Java 用户如何使用
`PropertyEditorManager.registerEditor(MyType.class, MyEditor.class)`；`findEditor(type)` 获取编辑器。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyEditorManager.java`；IDE 属性编辑器查找。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：register/find/unregister 及搜索路径 set/get。
