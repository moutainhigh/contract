package com.haier.hailian.contract.dto.grab;

import com.haier.hailian.contract.dto.ReservePlanResultDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 19033323
 */
@Data
@ApiModel(value = "创单节点抢单查看页面返回数据")
public class CDGrabViewResponseDto {
    @ApiModelProperty(value="分享比例",name="分享比例",example="5%")
    private String sharePercent;
    @ApiModelProperty(value="达成目标预计分享酬",name="达成目标预计分享酬",example="3000")
    private String targetShareMoney;
    @ApiModelProperty(value="链群目标",name="链群目标",example="400")
    private String chainGoal;
    @ApiModelProperty(value="链群抢单目标",name="链群抢单目标",example="450")
    private String chainGrabGoal;
    @ApiModelProperty(value="链群名称",name="链群名称")
    private String chainName;
    @ApiModelProperty(value="合约名称",name="合约名称")
    private String contractName;
    @ApiModelProperty(value="合约有效期开始",name="合约有效期开始")
    private String startTime;
    @ApiModelProperty(value="合约有效期结束",name="合约有效期结束")
    private String endTime;
    @ApiModelProperty(value="链群主编码",name="链群主编码")
    private String masterCode;
    @ApiModelProperty(value="链群主名字",name="链群主名字")
    private String masterName;
    @ApiModelProperty(value="小微编码",name="小微编码")
    private String xwCode;
    @ApiModelProperty(value="小微名字",name="小微名字")
    private String xwName;
    @ApiModelProperty(value="最小作战单元名字",name="最小作战单元名字")
    private String littleXWName;
    @ApiModelProperty(value="链群主是否已复核",name="链群主是否已复核",example="true")
    private String isReviewed = "false";
    @ApiModelProperty(value="预案信息",name="预案信息")
    private List<ReservePlanResultDTO> planList = new ArrayList<>();
    @ApiModelProperty(value="目标列表",name="目标列表")
    private List<CDGrabTargetDto> targetList = new ArrayList<>();
}
