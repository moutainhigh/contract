package com.haier.hailian.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 19012964
 * @since 2019-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TOdsMinbu extends Model<TOdsMinbu> {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 小微编码
     */
    @TableField("xwCode")
    private String xwCode;

    /**
     * 小微名称
     */
    @TableField("xwName")
    private String xwName;

    /**
     * 小微主工号
     */
    @TableField("xwMasterCode")
    private String xwMasterCode;

    /**
     * 小微主姓名
     */
    @TableField("xwMasterName")
    private String xwMasterName;

    /**
     * 最小作战单元编码（小小微编码）
     */
    @TableField("littleXwCode")
    private String littleXwCode;

    /**
     * 最小作战单元名称（小小微名称）
     */
    @TableField("littleXwName")
    private String littleXwName;

    /**
     * 最小作战单元主编码（小小微主编码）
     */
    @TableField("littleXwMasterCode")
    private String littleXwMasterCode;

    /**
     * 最小作战单元主姓名（小小微主姓名）
     */
    @TableField("littleXwMasterName")
    private String littleXwMasterName;

    /**
     * 所属平台编码
     */
    @TableField("ptCode")
    private String ptCode;

    /**
     * 所属平台名称
     */
    @TableField("ptName")
    private String ptName;

    /**
     * 小微类型3（研发、服务、制造...）
     */
    @TableField("xwType3")
    private String xwType3;
    @TableField("xwType3Code")
    private String xwType3Code;

    /**
     * 小微类型5（创单、体验）
     */
    @TableField("xwType5")
    private String xwType5;

    @TableField("xwType5Code")
    private String xwType5Code;

    /**
     * 对应用户小微编码
     */
    @TableField("userXwCode")
    private String userXwCode;

    /**
     * 对应用户小微名称
     */
    @TableField("userXwName")
    private String userXwName;

    /**
     * 区域编码
     */
    @TableField(exist = false)
    private String regionCode;

    /**
     * 区域名称
     */
    @TableField(exist = false)
    private String regionName;
    @TableField(exist = false)
    private boolean in42Center;
    @TableField(exist = false)
    private String calLevel;
    private Date createTime;

    @TableField(exist = false)
    private String chainCode;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
