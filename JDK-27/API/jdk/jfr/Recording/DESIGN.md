# jdk.jfr.Recording 设计案

## 1. API 功能
录制会话，管理一次 JFR 录制：构造/start/stop/dump/close，查询 id/state/name/settings/size。

## 2. Java 用户如何使用
new Recording() → enable → start → stop → dump(Path) → close，查询状态机 NEW→RUNNING→STOPPED。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/Recording.java；test/jdk/jdk/jfr/ 中 Recording 相关用例。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 public/关键方法；黑盒状态机 + name/settings + dump 非空 + size。
