# XMLFilter — 设计案

## 1. API 功能
SAX2 过滤接口：getParent/setParent + 继承 XMLReader 全部行为。

## 2. Java 用户如何使用
在 reader 之上插事件过滤层。

## 3. 开源代码实际用法调研
SAX 2.0.2 javadoc（filter 链规范）。

## 4. 测试用例设计 + 自动化实现
黑盒：getParent/setParent 往返、初值 null；自定义 filter 链（child→parent→真 reader）事件逐级转发；孤儿 filter（无 parent）parse → NPE；接口方法集锁定。实现见 helpers.XMLFilterImpl。
