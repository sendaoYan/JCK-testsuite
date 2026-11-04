# JarURLConnection 设计文档

## 一、API 功能

抽象类，表示到 jar 文件或 jar 内条目的 URL 连接，可访问 jar 文件、清单(Manifest)、条目与证书。

## 二、Java 用户如何使用

用户把 jar:...!/... URL 的 openConnection() 结果强转为 JarURLConnection，调用 getJarFile()/getManifest()/getJarEntry()/getAttributes()/getCertificates()。

## 三、开源代码实际用法调研

应用服务器、类加载器、构建工具读取 jar 内资源与元数据时使用。

## 四、测试用例设计

子类暴露受保护构造器验证 jarFileURL/entryName 解析(含百分号解码与无 !/ 抛 MalformedURLException)；用临时 jar 验证 getJarFile/getManifest/getJarEntry/getMainAttributes/getAttributes/getCertificates/getInputStream。
