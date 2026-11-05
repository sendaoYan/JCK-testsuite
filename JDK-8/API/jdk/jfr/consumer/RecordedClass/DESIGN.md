# jdk.jfr.consumer.RecordedClass 设计案（JDK-8）

## 1. API 功能
类引用值对象：`getName`/`getId`/`getModifiers`/`getClassLoader`。

## 2. Java 用户如何使用
事件中 Class 字段与栈帧的类身份。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedClass.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（Class 字段 + 栈帧双通道取类）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 27。
### 行为闭环
- 事件 Class 字段与自定义事件栈帧中的类：`getName()` 带 8 特有斜杠形式（如 `RecorderTest` 或 `java/lang/String` 形式，实测断言按测试类自身）；`getId()` 为正且跨字段/跨帧稳定（同一次录制内同一类同 id）。
- `getModifiers()` 与 `Class.getModifiers()` 一致；`getClassLoader()` 非空。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `getClass("nope")`（RecordedObject 通道，不存在字段） | IllegalArgumentException | `Could not find field with name nope` |
### 8/27 差异
8 侧类名为斜杠形式（`sun/misc/Launcher$AppClassLoader` 风格）；27 侧类加载器名为 "app"、类名带模块语义。
