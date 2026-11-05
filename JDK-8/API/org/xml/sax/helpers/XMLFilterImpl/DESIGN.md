# XMLFilterImpl — 设计案

## 1. API 功能
XMLFilter 默认实现：可作 reader 或 filter，事件全转发，子类覆写实现过滤。

## 2. Java 用户如何使用
事件流中间件（脱敏、元素裁剪、日志）。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：构造即挂 parent；五 handler 槽默认 null；真解析事件 parent→filter→handler 保序转发；filter 层 EntityResolver 拦截（systemId 已绝对化、以文件名结尾断言）、null 返回回退默认解析；子类按 qName 裁剪元素（drop 消失、keep 保留）；两级链端到端；孤儿 filter：parse→NPE、set/getFeature/setProperty→SAXNotRecognizedException。
