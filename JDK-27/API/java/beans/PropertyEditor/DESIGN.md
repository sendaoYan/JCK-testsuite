# java.beans.PropertyEditor — 设计案

## 1. API 功能
属性编辑器接口。setValue/getValue/isPaintable/paintValue/getJavaInitializationString/getAsText/setAsText/getTags/getCustomEditor/supportsCustomEditor/add/removePropertyChangeListener。

## 2. Java 用户如何使用
属性表格按属性类型查找 PropertyEditor 做文本/组件编辑。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyEditor.java`；`PropertyEditorManager` 注册的编辑器实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 关键方法。黑盒（`testBlackBox`）：PropertyEditorSupport 基本 set/get/getAsText 与默认值。

## 5. 深化覆盖（批次 beansF，2026-11，88 断言）
参考来源补全：Oracle JDK 27 javadoc（java.desktop/java.beans.PropertyEditor）；JavaBeans spec 1.01-A 第 7 章；OpenJDK `test/jdk/java/beans/PropertyEditor/Test*{Class,Type}{Java,Null,Value}.java`、`TestEnumClass*` 回归思路。

新增测试方法：testInterfaceInventory（恰 12 方法 + 逐方法签名断言：返回类型/形参类型/setAsText 声明 IAE/getTags→String[]/getCustomEditor→Component）、testSupportImplementor（经 PES 的接口契约）、testIntegerEditorChain（42/"42" 往返、setAsText("7")、init "7"、decode "0x1F"→31、setAsText(null)→null/“null”、垃圾文本→**NumberFormatException（实测精确类，IAE 子类）**、tags/custom/paint 默认）、testEditorIndependenceAndEvents（findEditor 每次新实例、PCL fire-per-setValue、事件 name/old/new null、source 为编辑器、remove 生效）、testBooleanEditorChain（"True"/"False"、tags、大小写不敏感、setAsText(null) 清值、非法→IAE、init 三态）、testStringEditorChain（引号与 `\"` `\\` `\n` 转义、null 值可 setAsText）、testEnumEditorChain（嵌套枚举免注册、tags=常量名、setAsText 往返、setValue 非枚举→IAE、null 合法）。

JDK-27 与 JDK-8 行为一致（默认编辑器同在 com.sun.beans.editors，模块内可访问）；Color/Font 编辑器 headless 下实例化失败→null（不断言，记录）。
