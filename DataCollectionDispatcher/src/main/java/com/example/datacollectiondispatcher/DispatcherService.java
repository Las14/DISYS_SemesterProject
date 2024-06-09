package com.example.datacollectiondispatcher;

import com.example.datacollectiondispatcher.entity.StationEntity;
import com.example.datacollectiondispatcher.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatcherService {
    private final StationRepository stationRepository;

    @Autowired
    public DispatcherService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    List<StationEntity> fetchStations() {
        return (List<StationEntity>) stationRepository.findAll();
    }
}
