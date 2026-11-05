# jdk.jfr.Configuration 设计案

## 1. API 功能
JFR 配置：getConfigurations/getConfiguration，name/settings/contents。

## 2. Java 用户如何使用
Configuration.getConfiguration("default").getSettings() 读取默认设置。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/Configuration.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 default 配置 + settings/contents 非空。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 内置配置表：default{provider "Oracle", name/label/description 非空, contents 非空, settings 非空且含 jdk.*#enabled 键}；profile 同构；两者 settings 不同；getConfigurations&gt;=2 且 provider 非空。
- create(Reader/Path) 闭环：最小 jfc（configuration version="2.0" + event + setting，格式采自 $JAVA_HOME/lib/jfr/default.jfc）→ label/provider/description 回读、name==null（文件创建无名）、setting 值逐字（"true"/"3 ms"）；Path 版一致；构造的 Configuration 可直接驱动 new Recording(cfg)。
负例精确异常表：
| 操作 | 异常 | 消息 |
|---|---|---|
| create(非 XML 内容) | ParseException | Error reading JFC file. |
| create((Reader)null) | NullPointerException | — |
| getConfiguration("bogus") | NoSuchFileException | Could not locate configuration with name bogus |
| getConfiguration(null) | 版本差异：8→NoSuchFileException（消息含 null）/ 27→NPE "name" | |
