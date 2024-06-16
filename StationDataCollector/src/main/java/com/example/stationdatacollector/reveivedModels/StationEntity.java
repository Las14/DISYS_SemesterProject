package com.example.stationdatacollector.reveivedModels;

public class StationEntity {
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    private String dbUrl;

    public void setDbUrl(String url) {
        this.dbUrl = url;
    }

    public String getDbUrl() {
        return this.dbUrl;
    }


    private Long lat;

    public void setLat(Long lat) {
        this.lat = lat;
    }

    public long getLat() {
        return this.lat;
    }


    private Long lng;

    public void setLng(Long lng) {
        this.lng = lng;
    }

    public Long getLng() {
        return this.lng;
    }
}
