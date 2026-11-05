# jdk.jfr.Event 设计案

## 1. API 功能
所有 JFR 事件基类（abstract），子类用注解描述元数据，begin/end/commit 提交。

## 2. Java 用户如何使用
继承 Event，加 @Name/@Label/@Description 与字段，new → begin → 赋值 → end → commit。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/Event.java；test/jdk/jdk/jfr/ 事件类。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract/关键方法/继承；黑盒 begin/end/commit + 注解回读。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 注解元数据闭环：@Name/@Label/@Category/@Description 经 EventType 全等回读；@Enabled/@Threshold/@StackTrace 是 setting 派生注解：getAnnotation 一律 null（双侧实证），其值经 getSettingDescriptors 默认值回读（enabled=false/threshold="1 ms"/stackTrace=false）。
- 无录制容错：无录制 commit 静默不抛、isEnabled/shouldCommit false；无 end() 直接 commit 容忍。
- 录制闭环：enable+RUNNING 时 isEnabled/shouldCommit true；commit 2 条（含 begin/end 与不带 begin 两式）→dump→回读字段全等（long/String/double/boolean/int）、duration>=0、start<=end、thread 非 null、@StackTrace(false)→getStackTrace()==null。
- set() 索引契约（重要实证修正）：静态编译的 Event 子类 set(任意 index) 不做边界检查（-1/99 均不抛）——IOOBE 只发生在 EventFactory 生成的动态事件类上；类型不匹配在 set 时不抛。
- 27 侧另断言父类 jdk.internal.event.Event；8 侧父类即 jdk.jfr.Event 自身。
负例精确异常表：无（set 全容忍；commit 静默）。
