# jdk.nio.zipfs.ZipInfo 契约设计

## 1. API 功能
`ZipInfo` 是 zipfs 自带的诊断工具类：`main(String[])` 打印 zip 的条目表（名、大小、压缩后大小、方法、偏移）。无参调用打印 `Usage: java ZipInfo zfname`。它不在导出 API 契约内，但长期作为 zipfs 调试入口随模块发布。

## 2. Java 用户如何使用
`java --add-modules jdk.zipfs -cp ... jdk.nio.zipfs.ZipInfo file.zip` 或测试中反射调用 main 捕获 stdout。打印内容逐条目、确定性。

## 3. 开源代码实际用法调研
- OpenJDK jdk.zipfs 测试目录自用；unzip -l / zipinfo(1) 是等价的系统层工具，输出布局不同但信息同构。
- 社区（如 zipfs 增强提案邮件列表）用它比对中央目录与本地头的差异。

## 4. 测试用例设计+自动化实现
- 反射 main（+open）：预制含 deep/entry.txt 的 zip，捕获 System.out，断言输出含条目名且长度可观（真实条目表，非空输出）。
- 无参分支：不抛异常、输出以 "Usage: java ZipInfo" 开头。
- 结构面：main 为 static public、单 String[] 参数。
- 自动化：jtreg `run main` + `check()`；stdout 捕获后恢复原流。
