# jdk.jfr.consumer.RecordedStackTrace 设计案（JDK-8）

## 1. API 功能
栈轨迹值对象：`getFrames`/`isTruncated`。

## 2. Java 用户如何使用
事件的 stackTrace 隐式字段；火焰图/剖析工具的原始材料。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedStackTrace.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒 + 黑盒（with/withoutStackTrace 矩阵 + 深栈截断）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 19。
### 行为闭环
- withStackTrace(true)（默认）：事件回读 stackTrace 非空，frames 非空且 frame0 在测试方法内。
- @WithoutStackTrace：stackTrace 为 null（矩阵对照）。
- 深递归制造超长栈：isTruncated()==true（录制设置 stackTrace 深度上限下截断）。
### 实测负例异常表
无专门负例（结构语义为主）。
### 8/27 差异
面一致；27 侧同样矩阵成立。
