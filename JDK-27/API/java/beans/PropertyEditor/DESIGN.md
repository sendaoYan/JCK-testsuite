# java.beans.PropertyEditor — 设计案

## 1. API 功能
属性编辑器接口。setValue/getValue/isPaintable/paintValue/getJavaInitializationString/getAsText/setAsText/getTags/getCustomEditor/supportsCustomEditor/add/removePropertyChangeListener。

## 2. Java 用户如何使用
属性表格按属性类型查找 PropertyEditor 做文本/组件编辑。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyEditor.java`；`PropertyEditorManager` 注册的编辑器实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 关键方法。黑盒（`testBlackBox`）：PropertyEditorSupport 基本 set/get/getAsText 与默认值。
