package com.haier.hailian.contract.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 19012964
 * @since 2019-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ZContracts extends Model<ZContracts> {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
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
    private LocalDateTime joinTime;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

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


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
