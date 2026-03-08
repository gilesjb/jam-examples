package org.copalis.jam.demo;

import com.google.gson.Gson;

public record Person(String name, int age) {

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Person fromJson(String json) {
        return new Gson().fromJson(json, Person.class);
    }
}