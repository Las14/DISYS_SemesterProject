package com.example.datacollectiondispatcher.repository;

import com.example.datacollectiondispatcher.entity.StationEntity;
import org.springframework.data.repository.CrudRepository;

public interface StationRepository  extends CrudRepository<StationEntity, Integer> {
}
