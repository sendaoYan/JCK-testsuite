# jdk.jfr.SettingDefinition 设计案（JDK-27 (temurin jdk-27.0.1.0.2+1)）

## 1. API 功能
设置定义注解（@Retention(RUNTIME) @Target(METHOD) marker）：把 SettingControl 挂到事件。

## 2. Java 用户如何使用
事件类里 @SettingDefinition boolean mySetting(MyControl c) { return true; }；方法名即设置名，@Name 可改名。

## 3. 开源代码实际用法调研
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒+黑盒合并，passed/check 骨架，末尾唯一一次 PASS 打印。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 元注解精确断言：RUNTIME + METHOD-only + 无成员（marker）。
- 正确签名（boolean 返回+控件参数）→ 描述符以方法名出现（+3 隐式 setting）。
- @Name("renamed") 于方法 → 描述符改名，原方法名不暴露。
- 错误签名（缺控件参数）→ 静默忽略：不抛异常、无描述符、仅剩 3 隐式。
- 一事件两个定义同时出现（3+2=5 项）。
- 定义出的 setting 可经 enable().with(name,value) 写入录制设置。
负例：无异常路径（错误签名静默忽略是本注解的关键契约）。
