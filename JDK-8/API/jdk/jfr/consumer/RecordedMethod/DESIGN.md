# jdk.jfr.consumer.RecordedMethod 设计案（JDK-8）

## 1. API 功能
方法引用值对象：`getType`/`getName`/`getDescriptor`/`getModifiers`。

## 2. Java 用户如何使用
栈帧 RecordedFrame.getMethod() 的返回类型；方法身份还原。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedMethod.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（受控调用栈的 frame0 方法身份）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 25。
### 行为闭环
- 自定义事件在受控方法内 commit：frame0 的 `getName()` 等于测试类方法名（jtreg MainWrapper 代理下 frame0 是测试类自身方法而非 main——实测语义）；`getType()` 是测试类；`getDescriptor()` 非空且含括号；`getModifiers()` 与反射 Method.getModifiers() 一致。
- 方法对象字段（type/name/descriptor/modifiers）经 getValue 通道可读。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
面一致；27 侧同样断言 frame0 方法身份，仅代理调用约定随 jtreg 版本相同。
