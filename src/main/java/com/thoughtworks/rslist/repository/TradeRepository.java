package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TradeRepository extends CrudRepository<TradeDto,Integer> {
    @Override
    List<TradeDto> findAll();
    @Transactional
    void deleteAllByRsEventId(int eventId);
}
