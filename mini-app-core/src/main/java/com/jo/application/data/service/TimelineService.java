package com.jo.application.data.service;

import com.jo.application.data.entity.ZJTItem;
import com.jo.application.data.repository.TimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimelineService {

    private final TimelineRepository generalRepository;

    @Autowired
    public TimelineService(TimelineRepository generalRepository) {
        this.generalRepository = generalRepository;
    }

    public List<ZJTItem> findAllByFilter(String groupId) {
        return generalRepository.findByGroupId(groupId);
    }

    public List<ZJTItem> findAll() {
        return generalRepository.findAll();
    }

    public void save(ZJTItem record) {
        generalRepository.save(record);
    }

    public void delete(ZJTItem record) {
        generalRepository.delete(record);
    }

}