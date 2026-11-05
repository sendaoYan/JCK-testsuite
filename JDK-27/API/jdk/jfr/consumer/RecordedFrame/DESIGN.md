# jdk.jfr.consumer.RecordedFrame 设计案

## 1. API 功能
记录的栈帧：getType/getMethod/isJavaFrame/getLineNumber。

## 2. Java 用户如何使用
RecordedStackTrace.getFrames().get(0)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedFrame.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 帧类型/方法/行号。

## 5. 深化（本轮）
断言规模：基线 → 60。
### 行为闭环
- 帧类型矩阵：getTypeFrame()/getJavaFrameType() 返回 "Interpreted"/"JIT compiled" 枚举串；isJavaFrame()==true。
- frame0/frame1 身份：方法名、行号>0、所属类逐项断言；同一方法跨帧身份稳定。
- 行号与帧深度语义：嵌套调用深度递增。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
27 帧类型断言更全（60 vs 29）；8 侧同构子集。
