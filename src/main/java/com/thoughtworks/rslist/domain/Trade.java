package com.thoughtworks.rslist.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Trade {
    @NotNull
    @Min(0)
    private int amount;
    @NotNull
    @Min(1)
    private int rank;
}
