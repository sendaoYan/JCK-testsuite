# jdk.jfr.RecordingState 设计案

## 1. API 功能
录制状态枚举：NEW/DELAYED/RUNNING/STOPPED/CLOSED。

## 2. Java 用户如何使用
Recording.getState() 返回该枚举。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/RecordingState.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 enum/5 常量；黑盒 valueOf/values/ordinal 顺序。

## 5. 深化（2026-11-05）：契约覆盖扩展
- values() 精确顺序+长度 5；ordinal 0..4；name/toString；compareTo 全序；valueOf 往返。
- values() 每次返回新数组（改副本不影响枚举本体）。
- Enum.valueOf 泛型通道与类特定 valueOf 一致。
- 真实 Recording 生命周期逐步核对枚举取值（NEW→RUNNING→STOPPED→CLOSED）。
负例精确异常表：valueOf("bogus")→IllegalArgumentException；valueOf(null)→NullPointerException。
