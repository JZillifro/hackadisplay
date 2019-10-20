package com.supplyframeproject.hackadisplay.model;

import lombok.Getter;

public class Project {
    @Getter
    int id;
    @Getter
    String url;
    @Getter
    int owner_id;
    @Getter
    String name;
    @Getter
    String summary;
    @Getter
    String description;
    @Getter
    String image_url;
    @Getter
    int views;
    @Getter
    int comments;
    @Getter
    int followers;
    @Getter
    int skulls;
    @Getter
    int logs;
    @Getter
    int details;
    @Getter
    int instruction;
    @Getter
    int components;
    @Getter
    int images;
    @Getter
    int created;
    @Getter
    int updated;
    @Getter
    String[] tags;
}
