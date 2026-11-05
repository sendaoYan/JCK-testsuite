# jdk.jfr.consumer.RecordedFrame 设计案（JDK-8）

## 1. API 功能
栈帧值对象：`getMethod`/`getLineNumber`/`getTypeFrame`/`getJavaFrameType`（8 侧名）/`isJavaFrame`。

## 2. Java 用户如何使用
栈帧序列逐帧还原调用位置与帧类型（Interpreted/Compiled/JIT 内联）。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedFrame.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（受控栈 frame0/frame1 身份与行号）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 29。
### 行为闭环
- frame0：方法名 == 提交事件的测试方法、行号 > 0 且与实际声明行同源（断言 >0 稳定）、帧类型为 "Interpreted" 或 "JIT compiled"（实测枚举串）、isJavaFrame()==true。
- frame1：调用者方法身份（嵌套调用结构断言）。
- 方法身份跨帧稳定：同一方法在不同帧中 method 对象字段一致。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
27 侧另有虚拟线程帧语义；帧类型枚举面一致（27 侧 60 断言含更全的帧类型矩阵）。
