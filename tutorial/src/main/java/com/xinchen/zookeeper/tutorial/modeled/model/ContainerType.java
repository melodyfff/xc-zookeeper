package com.xinchen.zookeeper.tutorial.modeled.model;

/**
 *
 * 容器类型
 *
 * @author xinchen
 * @version 1.0
 * @date 08/07/2019 09:04
 */

public class ContainerType {

    private final int typeId;

    public ContainerType(){
        this(0);
    }

    public ContainerType(int typeId) {
        this.typeId = typeId;
    }


    public int getTypeId() {
        return typeId;
    }


    @Override
    public int hashCode() {
        // typeId作为hash
        return typeId;
    }

    @Override
    public boolean equals(Object o) {

        // 重写equals判断,如果比较对象的 typeId相同也认为是同一对象

        if (o == this){
            return true;
        }

        if (null == o||getClass() != o.getClass()){
            return false;
        }

        ContainerType that = (ContainerType) o;
        return typeId == that.typeId;

    }

    @Override
    public String toString() {
        return "ContainerType{" +
                "typeId=" + typeId +
                '}';
    }
}
