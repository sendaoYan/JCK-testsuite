# jdk.jfr.consumer.RecordedClassLoader 设计案（JDK-8）

## 1. API 功能
类加载器值对象：`getName`/`getId`/`getType`（返回 RecordedClass）。

## 2. Java 用户如何使用
RecordedClass.getClassLoader() 的返回类型；加载器身份追踪。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedClassLoader.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（字段通道与栈帧通道交叉验证加载器 id）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 21。
### 行为闭环
- `getName()` 实测为 8 特有斜杠类名 `"sun/misc/Launcher$AppClassLoader"`（27 侧为 `"app"`）。
- `getType()` 非空且 getName() 与加载器类一致；`getId()` > 0。
- 交叉一致性：事件字段中类的加载器 id == 该类栈帧类的加载器 id（同加载器同 id）。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
名称形式：8 = 内部斜杠类名；27 = 语义短名 "app"，且 27 侧断言 id 跨录制稳定。
