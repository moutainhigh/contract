package com.haier.hailian.contract.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by 01431594 on 2019/12/19.
 */
@Data
public class GamblingContractDTO {

    private Integer id;
    /**
     * 关联id
     */
    private Integer parentId;

    /**
     * 合约名称
     */
    private String contractName;

    /**
     * 10链群主合约、20商圈合约、30创客合约
     */
    private String contractType;

    /**
     * 状态，0抢入中，1抢入成功,（已审批），2已驳回，3：被踢出,4:已过期
     */
    private String status;

    /**
     * 分享空间
     */
    private BigDecimal shareSpace;

    /**
     * 链群编码
     */
    private String chainCode;

    /**
     * 地区编码
     */
    private String regionCode;

    /**
     * 抢入截止时间
     */
    private String joinTime;

    /**
     * 开始时间
     */
    private String startDate;

    /**
     * 结束时间
     */
    private String endDate;

    /**
     * xw编码
     */
    private String xiaoweiCode;

    /**
     * 创建人编码
     */
    private String createCode;

    /**
     * 创建人姓名
     */
    private String createName;

    /**
     * 创建日期
     */
    private LocalDateTime createTime;

    private String orgCode;

    private String orgName;

    private List<ChainGroupTargetDTO> chainGroupTargetList;

    private List<MarketTargetDTO> marketTargetList;

}
