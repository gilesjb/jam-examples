package org.copalis.jam.demo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PersonTest {

    @Test
    void toJson_producesCorrectJson() {
        Person person = new Person("Alice", 30);
        String json = person.toJson();
        assertEquals("{\"name\":\"Alice\",\"age\":30}", json);
    }

    @Test
    void fromJson_restoresObject() {
        String json = "{\"name\":\"Bob\",\"age\":25}";
        Person person = Person.fromJson(json);
        assertEquals("Bob", person.name());
        assertEquals(25, person.age());
    }

    @Test
    void roundTrip_preservesData() {
        Person original = new Person("Charlie", 40);
        Person restored = Person.fromJson(original.toJson());
        assertEquals(original.name(), restored.name());
        assertEquals(original.age(), restored.age());
    }
}