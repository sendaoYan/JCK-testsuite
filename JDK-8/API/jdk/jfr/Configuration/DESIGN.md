# jdk.jfr.Configuration 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
预置/自定义录制配置（.jfc XML）：getConfiguration/getConfigurations/create(Reader|Path)。

## 2. Java 用户如何使用
Configuration.getConfiguration("default") → new Recording(config)；或 create(Reader) 解析自定义 jfc 内容。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 ConfigurationTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.Configuration.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 内置配置表：default{provider "Oracle", name/label/description 非空, contents 非空, settings 非空且含 jdk.*#enabled 键}；profile 同构；两者 settings 不同；getConfigurations&gt;=2 且 provider 非空。
- create(Reader/Path) 闭环：最小 jfc（configuration version="2.0" + event + setting，格式采自 $JAVA_HOME/lib/jfr/default.jfc）→ label/provider/description 回读、name==null（文件创建无名）、setting 值逐字（"true"/"3 ms"）；Path 版一致；构造的 Configuration 可直接驱动 new Recording(cfg)。
负例精确异常表：
| 操作 | 异常 | 消息 |
|---|---|---|
| create(非 XML 内容) | ParseException | Error reading JFC file. |
| create((Reader)null) | NullPointerException | — |
| getConfiguration("bogus") | NoSuchFileException | Could not locate configuration with name bogus |
| getConfiguration(null) | 版本差异：8→NoSuchFileException（消息含 null）/ 27→NPE "name" | |
