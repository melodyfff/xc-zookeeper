package com.xinchen.zookeeper.tutorial.modeled.model;

import java.util.Objects;

/**
 * @author xinchen
 * @version 1.0
 * @date 08/07/2019 09:19
 */
public class PersonModel {
    private final PersonId id;
    private final ContainerType containerType;
    private final String firstName;
    private final String lastName;
    private final int age;

    public PersonModel(){
        this(new PersonId(), new ContainerType(), null, null, 0);
    }

    public PersonModel(PersonId id, ContainerType containerType, String firstName, String lastName, int age) {
        this.id = id;
        this.containerType = containerType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public PersonId getId() {
        return id;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }


    @Override
    public boolean equals(Object o) {

        // 重写equals

        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        PersonModel that = (PersonModel) o;
        return age == that.age &&
                Objects.equals(id, that.id) &&
                Objects.equals(containerType, that.containerType) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + containerType.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + age;
        return result;
    }

    @Override
    public String toString() {
        return "PersonModel{" +
                "id=" + id +
                ", containerType=" + containerType +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                '}';
    }
}
