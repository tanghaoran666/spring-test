package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.service.RsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired TradeRepository tradeRepository;
  @Autowired
  RsService rsService;
  private UserDto userDto;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    rsService.init();
    userDto =
            UserDto.builder()
                    .voteNum(10)
                    .phone("188888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("idolice")
                    .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    UserDto save = userRepository.save(userDto);

    for (int i = 0; i < 5; i++) {
      RsEventDto rsEventDto =
              RsEventDto.builder().keyword("无分类").eventName("第一条事件").voteNum(5-i).user(save).build();

      rsEventRepository.save(rsEventDto);
    }

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第三条事件").voteNum(3).user(save).build();

    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list"))
        .andExpect(jsonPath("$", hasSize(6)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[0]", not(hasKey("user"))))
            .andExpect(jsonPath("$[0].voteNum",is(5)))
            .andExpect(jsonPath("$[1].voteNum",is(4)))
            .andExpect(jsonPath("$[2].voteNum",is(3)))
            .andExpect(jsonPath("$[3].voteNum",is(3)))
            .andExpect(jsonPath("$[4].voteNum",is(2)))
            .andExpect(jsonPath("$[5].voteNum",is(1)))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }

  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll(Sort.by(Sort.Direction.DESC,"voteNum"));
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }

  @Test
  public void shouldTradeRankSuccess() throws Exception{

    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto1 =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto1 = rsEventRepository.save(rsEventDto1);

    Trade trade1 = Trade.builder().amount(1).rank(1).build();
    String jsonValue1 = objectMapper.writeValueAsString(trade1);

    mockMvc
            .perform(
                    post("/rs/buy/{id}", rsEventDto1.getId())
                            .content(jsonValue1)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());


    Trade trade2 = Trade.builder().amount(2).rank(1).build();
    String jsonValue2 = objectMapper.writeValueAsString(trade2);


    RsEventDto rsEventDto2 =
            RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventDto2 = rsEventRepository.save(rsEventDto2);
    mockMvc
            .perform(
                    post("/rs/buy/{id}", rsEventDto2.getId())
                            .content(jsonValue2)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    List<TradeDto> trades = tradeRepository.findAll();
    assertEquals(1,trades.size());
    assertEquals(2,trades.get(0).getAmount());
    assertEquals(1,trades.get(0).getRank());
    assertEquals(rsEventDto2.getId(),trades.get(0).getRsEvent().getId());
    assertEquals(1,rsEventRepository.findAll(Sort.by(Sort.Direction.DESC,"voteNum")).size());

  }
  @Test
  public void shouldThrowExceptionWhenAmountLess() throws Exception{
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    Trade trade = Trade.builder().amount(1).rank(1).build();
    String jsonValue = objectMapper.writeValueAsString(trade);

    mockMvc
            .perform(
                    post("/rs/buy/{id}", rsEventDto.getId())
                            .content(jsonValue)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());


    RsEventDto rsEventDto2 =RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventDto2 = rsEventRepository.save(rsEventDto2);
    mockMvc
            .perform(
                    post("/rs/buy/{id}", rsEventDto2.getId())
                            .content(jsonValue)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error",is("invlid trade amount")));

  }

  @Test
  public void shouldThrowExceptionWhenEventIdInvalid() throws Exception{
    Trade trade = Trade.builder().amount(1).rank(1).build();
    String jsonValue = objectMapper.writeValueAsString(trade);

    mockMvc
            .perform(
                    post("/rs/buy/{id}", 100)
                            .content(jsonValue)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error",is("invlid trade id")));
  }

  @Test
  public void shouldGetEventListHasVoteAndTrade() throws Exception {
    UserDto save = userRepository.save(userDto);

    for (int i = 0; i < 5; i++) {
      RsEventDto rsEventDto =
              RsEventDto.builder().keyword("无分类").eventName("无名事件").voteNum(5-i).user(save).build();

      rsEventRepository.save(rsEventDto);
    }

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("买的").eventName("叫我第一名").voteNum(2).user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);
    Trade trade = Trade.builder().rank(1).amount(10).build();
    rsService.buy(trade,rsEventDto.getId());

    mockMvc
            .perform(get("/rs/list"))
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[0].eventName", is("叫我第一名")))
            .andExpect(jsonPath("$[0].keyword", is("买的")))
            .andExpect(jsonPath("$[0].voteNum",is(2)))
            .andExpect(jsonPath("$[1].voteNum",is(5)))
            .andExpect(jsonPath("$[2].voteNum",is(4)))
            .andExpect(jsonPath("$[3].voteNum",is(3)))
            .andExpect(jsonPath("$[4].voteNum",is(2)))
            .andExpect(jsonPath("$[5].voteNum",is(1)))
            .andExpect(status().isOk());
  }

  @Test
  public void shouldDeleteEventBuId() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("无名事件").voteNum(5).user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);
    assertEquals(1,rsEventRepository.findAll(Sort.by(Sort.Direction.DESC,"voteNum")).size());
    mockMvc.perform(delete("/rs/{index}",rsEventDto.getId()))
            .andExpect(status().isOk());
    assertEquals(0,rsEventRepository.findAll(Sort.by(Sort.Direction.DESC,"voteNum")).size());

  }

}
