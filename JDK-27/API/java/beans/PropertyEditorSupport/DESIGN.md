# java.beans.PropertyEditorSupport — 设计案

## 1. API 功能
属性编辑器便利基类（implements PropertyEditor）。构造器 `()`/`(source)`；get/setSource、set/getValue、getAsText/setAsText、isPaintable/getJavaInitializationString、add/removePropertyChangeListener、firePropertyChange。

## 2. Java 用户如何使用
继承 PropertyEditorSupport 实现自定义编辑器，重写 getAsText/setAsText 等。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyEditorSupport.java`；大量编辑器继承之。

## 4. 测试用例设计 + 自动化实现
白盒：implements PropertyEditor。黑盒（`testBlackBox`）：默认值、String/非 String 值 asText/setAsText、setAsText 抛 IllegalArgumentException、source 与 firePropertyChange。
