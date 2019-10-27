package com.supplyframeproject.hackadisplay.model;

import lombok.Getter;

public class ImageSet {
    @Getter
    int total;
    @Getter
    int per_page;
    @Getter
    int last_page;
    @Getter
    int page;
    @Getter
    Image[] images;
}
