package com.haier.hailian.contract.dto;

import com.haier.hailian.contract.entity.ZReservePlanTeamwork;
import com.haier.hailian.contract.entity.ZReservePlanTeamworkDetail;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * (ZReservePlanTeamwork)实体类
 *
 * @author makejava
 * @since 2019-12-24 17:13:55
 */
@Data
@ApiModel(value = "ZReservePlanTeamworkDto", description = "并联交互预案")
public class ZReservePlanTeamworkDto implements Serializable {
    private static final long serialVersionUID = 992358289080385319L;
    //主键
    private Integer id;
    //合约id
    private Integer parentId;
    //问题编码
    private String problemCode;
    //问题描述
    private String problemContent;
    //问题类型
    private String problemType;
    //问题来源
    private String problemChannel;
    //任务发起部门
    private String problemDep;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;
    //提醒时间  任务定时提醒时间(0:不提醒、1:每天、2:每工作日、3:每周、4:每两周、5:每月)
    private String remindTime;
    //提醒类型  提醒类型：0不提醒，1截止提醒，2截止前1小时，3截止前1天
    private String remindType;
    //是否重复：0/1 - 是/否
    private String isRepeat;
    //是否重要：0/1 - 是/否
    private String isImportant;
    //执行者
    private String executer;
    //抄送者
    private String teamworker;
    //创建者编码
    private String createUserCode;
    //创建者名称
    private String createUserName;
    //创建时间
    private Date createUserTime;
    //任务编码
    private String taskCode;
    //群组的ID
    private String groupId;
    //问题的类型编码
    private String problemTypeId;
    //问题的来源编码
    private String problemChannelId;

    List<ZReservePlanTeamworkDetail> details;

}