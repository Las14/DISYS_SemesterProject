package com.example.datacollectiondispatcher;

import com.example.datacollectiondispatcher.entity.StationEntity;

import java.util.List;

public class CustomerStationData {
    private String customerId;
    private List<StationEntity> stations;

    public String getCustomerId() {
        return customerId;
    }

    public List<StationEntity> getStations() {
        return stations;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setStations(List<StationEntity> stations) {
        this.stations = stations;
    }

    public CustomerStationData(String customerId, List<StationEntity> stations) {
        this.customerId = customerId;
        this.stations = stations;
    }
}