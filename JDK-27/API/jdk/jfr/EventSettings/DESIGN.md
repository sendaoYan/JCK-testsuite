# jdk.jfr.EventSettings 设计案

## 1. API 功能
事件设置（Recording.enable 返回）：with/withoutThreshold、with/withoutStackTrace、withPeriod。

## 2. Java 用户如何使用
r.enable("jdk.FileRead").withThreshold(Duration)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/EventSettings.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 enable 返回 + 链式 with/without 调用。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 值格式实测表：withThreshold(3ms)→"3000000 ns"；withoutThreshold→"0 s"；withPeriod(5s)→"5000000000 ns"；withStackTrace()→"true"；withoutStackTrace→"false"；with(name,value) 逐字写入。
- 链式持久化：多步链后 getSettings 各键均在；disable 只置 enabled="false"（其他键保留）。
- 自定义 setting 传播：with("flavor","vanilla") → 录制设置键 "test.X#flavor" 全等。
- 版本差异：with(null name, value) 8 侧容忍不抛、27 侧 NPE。
负例精确异常表：with(name, null value)→NullPointerException（双侧）；with(null name, value)：8 无异常 / 27 NullPointerException。
