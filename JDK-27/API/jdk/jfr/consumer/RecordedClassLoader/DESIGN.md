# jdk.jfr.consumer.RecordedClassLoader 设计案

## 1. API 功能
记录的类加载器（final extends RecordedObject）：getName/getId/getType。

## 2. Java 用户如何使用
从 RecordedClass.getClassLoader() 获取。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedClassLoader.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 final/extends RecordedObject/关键方法；黑盒 获取类加载器名称（可空则跳过）。

## 5. 深化（本轮）
断言规模：基线 → 18。
### 行为闭环
- 27 实测 `getName() == "app"`（应用类加载器语义名）。
- `getType()` 非空且名称一致；`getId()` 跨两次独立录制稳定（27 侧实证）。
- 字段通道与栈帧通道加载器 id 一致。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
8 侧名称为 "sun/misc/Launcher$AppClassLoader"（斜杠内部名）；27 为短名 "app"。
