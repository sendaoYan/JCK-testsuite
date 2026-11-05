# jdk.jfr.consumer.RecordedClass 设计案

## 1. API 功能
记录的事件类（final extends RecordedObject）：getName/getId/getModifiers/getClassLoader。

## 2. Java 用户如何使用
从 RecordedEvent 栈帧方法 getType() 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedClass.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/关键方法；黑盒 录制自定义事件 → 栈帧 → 类名/id/modifiers。

## 5. 深化（本轮）
断言规模：基线 → 22。
### 行为闭环
- 事件 Class 字段与栈帧双通道取类：同一次录制内同一类 getId() 稳定（"same class id across fields and frames"）。
- getModifiers() 与反射一致；getClassLoader() 非空。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `getClass("nope")`（不存在字段） | IllegalArgumentException | `Could not find field with name nope` |
### 8/27 差异
27 类加载器名 "app"；8 为内部斜杠类名。
