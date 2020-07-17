package com.xinchen.zookeeper.tutorial.modeled.model;

import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;

/**
 *
 *
 * A full specification for dealing with a portion of the ZooKeeper tree. ModelSpec's contain:
 * 处理ZooKeeper树的一部分的完整规范。 ModelSpec包含:
 *
 * <ul>
 *     <li>A node path</li>
 *     <li>Serializer for the data stored</li>
 *     <li>Options for how to create the node (mode, compression, etc.)</li>
 *     <li>Options for how to deleting the node (quietly, guaranteed, etc.)</li>
 *     <li>ACLs</li>
 *     <li>Optional schema generation</li>
 * </ul>

 * @author xinchen
 * @version 1.0
 * @date 08/07/2019 09:25
 */
public class PersonModelSpec {

    private final AsyncCuratorFramework client;
    private final ModelSpec<PersonModel> modelSpec;

    public PersonModelSpec(AsyncCuratorFramework client) {
        this.client = client;

        JacksonModelSerializer<PersonModel> serializer = JacksonModelSerializer.build(PersonModel.class);
        // 规定路径
        ZPath path = ZPath.parseWithIds("/example/{id}/path/{id}");
        // 创建ModelSpec
        modelSpec = ModelSpec.builder(path, serializer).build();
    }

    public ModeledFramework<PersonModel> resolved(ContainerType containerType,PersonId personId){
        // 解析
        ModelSpec<PersonModel> resolved = modelSpec.resolved(containerType.getTypeId(), personId.getId());
        return ModeledFramework.wrap(client, resolved);
    }
}
