package com.thoughtworks.rslist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;
  private List<Integer> tradeAmountForRank = new ArrayList<>();
  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public void buy(Trade trade, int id) {
    tradeAmountForRank.add(0);
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(id);
    if(!rsEventDto.isPresent()){
      throw new RequestNotValidException("invlid trade id");
    }

    if(trade.getAmount()<=tradeAmountForRank.get(trade.getRank()-1)){
      throw new RequestNotValidException("invlid trade amount");
    }
    RsEventDto rsEvent = rsEventDto.get();
    TradeDto tradeDto = TradeDto.builder().amount(trade.getAmount())
            .rank(trade.getRank())
            .rsEvent(rsEvent)
            .build();
    tradeRepository.save(tradeDto);
    tradeAmountForRank.set(trade.getRank()-1,trade.getAmount());
    if(rsEventRepository.findByTradeRank(trade.getRank()).isPresent()){
      tradeRepository.deleteAllByRsEventId(rsEventRepository.findByTradeRank(trade.getRank()).get().getId());
      rsEventRepository.deleteAllByTradeRank(trade.getRank());
    }
    rsEvent.setTradeRank(trade.getRank());
    rsEventRepository.save(rsEvent);
  }

  public void postEvent(RsEvent rsEvent) {
    Optional<UserDto> userDto = userRepository.findById(rsEvent.getUserId());
    if (!userDto.isPresent()) {
      throw new RequestNotValidException("invalid userId");
    }
    RsEventDto build =
            RsEventDto.builder()
                    .keyword(rsEvent.getKeyword())
                    .eventName(rsEvent.getEventName())
                    .voteNum(0)
                    .user(userDto.get())
                    .build();
    rsEventRepository.save(build);
    tradeAmountForRank.add(0);
  }

  public void init() {
    tradeRepository.deleteAll();
    voteRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
    tradeAmountForRank.clear();
  }

  public List<RsEvent> getEventList(Integer start, Integer end) {
    List<RsEvent> rsEvents =
            rsEventRepository.findAll().stream()
                    .map(
                            item ->
                                    RsEvent.builder()
                                            .eventName(item.getEventName())
                                            .keyword(item.getKeyword())
                                            .userId(item.getId())
                                            .voteNum(item.getVoteNum())
                                            .build())
                    .collect(Collectors.toList());
    if (start == null || end == null) {
      return rsEvents;
    }
    return rsEvents.subList(start - 1, end);
  }

  public RsEvent getEventByIndex(int index) {
    List<RsEvent> rsEvents =
            rsEventRepository.findAll().stream()
                    .map(
                            item ->
                                    RsEvent.builder()
                                            .eventName(item.getEventName())
                                            .keyword(item.getKeyword())
                                            .userId(item.getId())
                                            .voteNum(item.getVoteNum())
                                            .build())
                    .collect(Collectors.toList());
    if (index < 1 || index > rsEvents.size()) {
      throw new RequestNotValidException("invalid index");
    }
    return rsEvents.get(index - 1);
  }
}
