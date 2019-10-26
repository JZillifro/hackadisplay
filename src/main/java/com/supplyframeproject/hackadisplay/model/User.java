package com.supplyframeproject.hackadisplay.model;

import lombok.Getter;
import lombok.Setter;

public class User {

    @Getter
    int id;

    @Getter
    String url;

    @Getter
    String username;

    @Getter
    @Setter
    String screen_name;

    @Getter
    int rank;

    @Getter
    String image_url;

    @Getter
    int followers;

    @Getter
    int following;

    @Getter
    int projects;
    @Getter
    int skulls;

    @Getter
    int pages;

    @Getter
    String location;

    @Getter
    String about_me;

    @Getter
    String who_am_i;

    @Getter
    String what_i_have_done;

    @Getter
    String what_i_would_like_to_do;

    @Getter
    int created;

    @Getter
    String[] tags;
}
