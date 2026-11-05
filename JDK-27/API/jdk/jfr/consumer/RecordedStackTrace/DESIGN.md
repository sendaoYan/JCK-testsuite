# jdk.jfr.consumer.RecordedStackTrace 设计案

## 1. API 功能
记录的栈轨迹：getFrames/isTruncated。

## 2. Java 用户如何使用
RecordedEvent.getStackTrace()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedStackTrace.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 frames 非空 + isTruncated 可调用。

## 5. 深化（本轮）
断言规模：基线 → 15。
### 行为闭环
- with/withoutStackTrace 矩阵：默认栈非空且 frame0 在提交方法内；@WithoutStackTrace 事件 stackTrace 为 null。
- 深递归超限栈 isTruncated()==true。
### 实测负例异常表
无专门负例（结构语义为主）。
### 8/27 差异
面一致。
