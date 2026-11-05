# jdk.jfr.Event 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
用户事件基类：实例字段即事件载荷，begin/end/commit 生命周期、isEnabled/shouldCommit、set(int,Object) 索引赋值。

## 2. Java 用户如何使用
static 嵌套类 extends Event + @Name/@Label/@Category/@Description/@Enabled/@Threshold/@StackTrace 注解；填字段后 commit。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 EventTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.Event.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 注解元数据闭环：@Name/@Label/@Category/@Description 经 EventType 全等回读；@Enabled/@Threshold/@StackTrace 是 setting 派生注解：getAnnotation 一律 null（双侧实证），其值经 getSettingDescriptors 默认值回读（enabled=false/threshold="1 ms"/stackTrace=false）。
- 无录制容错：无录制 commit 静默不抛、isEnabled/shouldCommit false；无 end() 直接 commit 容忍。
- 录制闭环：enable+RUNNING 时 isEnabled/shouldCommit true；commit 2 条（含 begin/end 与不带 begin 两式）→dump→回读字段全等（long/String/double/boolean/int）、duration>=0、start<=end、thread 非 null、@StackTrace(false)→getStackTrace()==null。
- set() 索引契约（重要实证修正）：静态编译的 Event 子类 set(任意 index) 不做边界检查（-1/99 均不抛）——IOOBE 只发生在 EventFactory 生成的动态事件类上；类型不匹配在 set 时不抛。
- 27 侧另断言父类 jdk.internal.event.Event；8 侧父类即 jdk.jfr.Event 自身。
负例精确异常表：无（set 全容忍；commit 静默）。
