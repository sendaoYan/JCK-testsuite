# jdk.jfr.EventSettings 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
事件设置构建器：withThreshold/withPeriod/withStackTrace/without*/with(name,value) 链式写入录制设置。

## 2. Java 用户如何使用
recording.enable("test.X").withThreshold(...).withPeriod(...)… 每步返回新 EventSettings 可继续链。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 EventSettingsTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.EventSettings.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 值格式实测表：withThreshold(3ms)→"3000000 ns"；withoutThreshold→"0 s"；withPeriod(5s)→"5000000000 ns"；withStackTrace()→"true"；withoutStackTrace→"false"；with(name,value) 逐字写入。
- 链式持久化：多步链后 getSettings 各键均在；disable 只置 enabled="false"（其他键保留）。
- 自定义 setting 传播：with("flavor","vanilla") → 录制设置键 "test.X#flavor" 全等。
- 版本差异：with(null name, value) 8 侧容忍不抛、27 侧 NPE。
负例精确异常表：with(name, null value)→NullPointerException（双侧）；with(null name, value)：8 无异常 / 27 NullPointerException。
