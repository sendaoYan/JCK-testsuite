# jdk.jfr.SettingControl 设计案

## 1. API 功能
设置控件基类（abstract）：combine/setValue/getValue。

## 2. Java 用户如何使用
匿名子类实现 setValue/getValue/combine。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/SettingControl.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract/关键方法；黑盒 匿名实现 setValue → getValue → combine。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 抽象面：恰好 3 个抽象方法；不能直接实例化（InstantiationException）；protected 默认构造器存在。
- 自定义控件行为：setValue/getValue 往返、combine 单值/多值全见、setValue 存任意串（值校验是实现自定义契约）。
- 与事件集成闭环：@SettingDefinition 方法暴露 SettingDescriptor（默认值=新控件 getValue()="none"；typeName=控件类名）；enable().with("flavor",…) 传播到录制设置。
- 版本差异（白盒）：8 侧 extends jdk.jfr.internal.Control；27 侧直接 extends Object（内部基类被移除）。
- 27 侧反射限制：对 jdk.jfr 受保护构造器 setAccessible 会 InaccessibleObjectException（模块未 opens）——只断言构造器存在。
负例精确异常表：Class.newInstance()→InstantiationException。
