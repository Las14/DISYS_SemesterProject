package com.example.datacollectiondispatcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "station")
public class StationEntity {
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    @Id
    public int getId() {
        return id;
    }

    @Column(name="db_url")
    private String dbUrl;

    public void setDbUrl(String url) {
        this.dbUrl = url;
    }

    public String getDbUrl() {
        return this.dbUrl;
    }

    @Column(name="lat")
    private Long lat;

    public void setLat(Long lat) {
        this.lat = lat;
    }

    public long getLat() {
        return this.lat;
    }

    @Column(name="lng")
    private Long lng;

    public void setLng(Long lng) {
        this.lng = lng;
    }

    public Long getLng() {
        return this.lng;
    }
}
