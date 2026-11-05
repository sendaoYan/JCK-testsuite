# javax.xml.stream.XMLResolver — 设计案（JDK-8）

## 1. API 功能
实体解析回调接口（resolveEntity(publicId, systemId, baseURI, namespace) → Object：InputStream/String）。

## 2. 使用方式
实现 resolveEntity 后 XMLInputFactory.setXMLResolver 安装。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 拦截闭环（离线）
XMLInputFactory.setXMLResolver + IS_SUPPORTING_EXTERNAL_ENTITIES=true + DOCTYPE 外部实体 → resolver 被调（参数 (null publicId, systemId, null baseURI, null ns) 实测）→ 返回 ByteArrayInputStream 替代内容 `<sub>RESOLVED</sub>` → 解析事件序列 [11,1,1,4,2,2,8]、文本 "RESOLVED"（替代内容真实进入树——核心闭环，绝不真联网）。
### 边界
- 内部（内联 DTD 子集）实体**不经过 resolver**（调用计数 0）。
- resolver 返回 null：回退默认解析（打开 systemId 文件）→ 不存在文件确定性抛 XMLStreamException。
- 未装 resolver + 默认 IS_SUPPORTING_EXTERNAL_ENTITIES=false：引用外部实体但未实际展开的文档照常离线解析。
### 发现
getXMLResolver() 返回已装实例。

### 断言计数
见 XMLResolverTest.java 末尾 PASS 输出（本轮深化后 11 断言，双侧只增不减）。
