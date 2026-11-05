# javax.xml.transform.dom(子包目录级)— 设计案

## 1. API 功能
TrAX 的 DOM 桥:DOMSource(Source 侧)、DOMResult(Result 侧)、DOMLocator(SourceLocator 子接口,附 originating-node)。常量 FEATURE 各类一份("http://javax.xml.transform.dom.DOMSource/feature 与 dom.DOMResult/feature")。JDK-27 的 DOMSource 覆写 isEmpty()。

## 2. Java 用户如何使用
DOM 树直接进出 transform():`transform(new DOMSource(doc), new DOMResult())`;错误回调里经 DOMLocator 拿出错节点。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 DOM 往返与定位用例;JDK 内部 DOMResult 校验 nextSibling 包含关系(抛 IAE/ISE,消息以 "Cannot create a DOMResult when…" 开头)。

## 4. 测试用例设计 + 易错细节(目录级汇总)
三个类各自测试文件见同目录子目录。子包级闭环:DOM→Stream / Stream→DOM / DOM→DOM 三向恒等矩阵;构造矩阵 + nextSibling 约束负例(IAE "not contained" / ISE "null node");JDK-27 版本面:DOMSource.isEmpty()——null node/新文档 true、已解析树或 setNode 后 false(区别于 JDK-8 无此方法);DOMLocator 恰 4+1 方法。
易错:createElement 不挂树,nextSibling 校验即失败;JDK-27 setNode(null) 前必须先清 nextSibling。
