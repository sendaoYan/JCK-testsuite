# jdk.jfr.SettingControl 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
自定义设置控件抽象基类：combine(Set&lt;String&gt;)/setValue(String)/getValue() 三抽象方法。

## 2. Java 用户如何使用
extends SettingControl 实现三方法；事件类用 @SettingDefinition 方法挂接。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 SettingControlTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.SettingControl.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 抽象面：恰好 3 个抽象方法；不能直接实例化（InstantiationException）；protected 默认构造器存在。
- 自定义控件行为：setValue/getValue 往返、combine 单值/多值全见、setValue 存任意串（值校验是实现自定义契约）。
- 与事件集成闭环：@SettingDefinition 方法暴露 SettingDescriptor（默认值=新控件 getValue()="none"；typeName=控件类名）；enable().with("flavor",…) 传播到录制设置。
- 版本差异（白盒）：8 侧 extends jdk.jfr.internal.Control；27 侧直接 extends Object（内部基类被移除）。
- 27 侧反射限制：对 jdk.jfr 受保护构造器 setAccessible 会 InaccessibleObjectException（模块未 opens）——只断言构造器存在。
负例精确异常表：Class.newInstance()→InstantiationException。
