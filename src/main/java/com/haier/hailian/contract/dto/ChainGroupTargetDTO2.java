package com.haier.hailian.contract.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by 01431594 on 2019/12/20.
 */
@Data
public class ChainGroupTargetDTO2 {

    private String targetCode;

    private String targetName;

    private String targetUnit;

    private String isLqTarget;

    private BigDecimal bottom;

    private BigDecimal E2E;

    private BigDecimal grab;

}
