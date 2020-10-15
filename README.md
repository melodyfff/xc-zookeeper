# xc-zookeeper
## Tutorial List
### tutorial-curator
Zookeeper Tutorial Base On Curator

|||
|---|---|
|/leader|Example leader selector code|
|/cache|Example PathChildrenCache usage|
|/locking|Example of using InterProcessMutex|
|/discovery|Example usage of the Curator's ServiceDiscovery|
|/framework|A few examples of how to use the CuratorFramework class|
|/async|Example AsyncCuratorFramework code|
|/modeled|ModeledFramework and Modeled Cache examples|

本次使用`Curator-4.2.*`针对`zookeeper-3.5.*`版本

###tutorial-zookeeper-client
官网资料： https://github.com/apache/zookeeper/tree/branch-3.4.14/zookeeper-docs/src/main/resources/markdown

---

## Cloud List
### spring cloud zookeeper
```bash
- service : 服务注册中心,服务提供者
- client : 消费者,Fegin
- config : 配置中心
- gateway : 网关服务
- zuul : 网关服务zuul
``` 

需要提前在`zk`中执行初始化命令
```bash
create /config/xc-zookeeper-config,dev/hello.message "Hello World from dev profile"
create /config/xc-zookeeper-config,test/hello.message "Hello World from dev profile"
```

---

## Reference

### zookeeper超级管理员
修改`zkServer.sh`中
```bash
nohup $JAVA "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}"
```
添加

```bash
"-Dzookeeper.DigestAuthenticationProvider.superDigest=super:xQJmxLMiHGwaqBvst5y6rkB6HQs="
```
重启后使用如下命令认证权限
```bash
#认证格式：addauth digest username:password(明文)
addauth digest super:admin
```

**认证密码的生成**
```bash
echo -n root:root | openssl dgst -binary -sha1 | openssl base64
qiTlqPLK7XM2ht3HMn02qRpkKIE=

# 或者使用zookeeper类库生成
java -cp /zookeeper-3.4.13/zookeeper-3.4.13.jar:/zookeeper-3.4.13/lib/slf4j-api-1.7.25.jar \
  org.apache.zookeeper.server.auth.DigestAuthenticationProvider \
  root:root
root:root->root:qiTlqPLK7XM2ht3HMn02qRpkKIE=
```

[Apache-Curator](http://curator.apache.org/curator-examples/index.html)

[curator-examples](http://curator.apache.org/curator-examples/index.html)

[使用Curator时对应的zookeeper版本](http://curator.apache.org/zk-compatibility.html)

[【zookeeper】ACL super 超级管理员](https://blog.csdn.net/u010900754/article/details/78498291)
