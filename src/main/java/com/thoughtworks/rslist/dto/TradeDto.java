package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade")
public class TradeDto {
    @Id
    @GeneratedValue
    private int id;

    private int amount;
    private int rank;
    @ManyToOne
    @JoinColumn(name = "rs_event_id")
    private RsEventDto rsEvent;
}
