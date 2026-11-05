# jdk.jfr.consumer.RecordedMethod 设计案

## 1. API 功能
记录的方法：getName/getDescriptor/getType/isHidden。

## 2. Java 用户如何使用
RecordedFrame.getMethod()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedMethod.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 方法名/描述符/声明类。

## 5. 深化（本轮）
断言规模：基线 → 20。
### 行为闭环
- 受控栈 frame0 方法身份：getName == 提交方法名、getType == 测试类、descriptor 非空含括号、modifiers 与反射一致。
- 方法对象字段经 getValue 通道可读（type/name/descriptor/modifiers）。
### 实测负例异常表
无专门负例（正值语义为主）。
### 8/27 差异
面一致（jtreg MainWrapper 代理约定双侧相同：frame0 是测试类方法而非 main）。
