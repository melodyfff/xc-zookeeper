package com.xinchen.zookeeper.tutorial.modeled.model;

import java.util.Objects;

/**
 * @author xinchen
 * @version 1.0
 * @date 08/07/2019 09:13
 */
public class PersonId {
    private final String id;


    public PersonId(){
        this("");
    }

    public PersonId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }


    @Override
    public boolean equals(Object o) {
        // 重写equals

        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        PersonId personId = (PersonId) o;
        return Objects.equals(id, personId.id);
    }

    @Override
    public int hashCode() {
        // 重写hash
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "PersonId{" +
                "id='" + id + '\'' +
                '}';
    }
}
