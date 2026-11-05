# jdk.jfr.SettingDescriptor 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
设置元数据：name/label/description/contentType/typeName/defaultValue/typeId/注解元素。

## 2. Java 用户如何使用
EventType.getSettingDescriptors() 获取；自定义 SettingControl 经 @SettingDefinition 产生。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 SettingDescriptorTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.SettingDescriptor.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 内建三件表（自定义事件上）：enabled{默认"true", contentType jdk.jfr.BooleanFlag, label/description 非空, typeId>0}；threshold{默认"0 ns", contentType jdk.jfr.Timespan, label "Threshold"}；stackTrace{默认"true", contentType jdk.jfr.BooleanFlag}。
- 自定义控件描述符：默认值=控件 getValue()；typeName=控件类名；label/description/contentType 均 null；annotationElements 空；getAnnotation(Label/Timespan) null（setting 派生注解不走 getAnnotation）。
- 全部描述符 name 非空、typeName 非空、typeId&gt;0。
负例：无（纯读 API）。
