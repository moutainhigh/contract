package com.haier.hailian.contract.controller.homepage;

import com.haier.hailian.contract.dto.R;
import com.haier.hailian.contract.dto.homepage.ChainGroupInfoDto;
import com.haier.hailian.contract.dto.homepage.ContractListRes;
import com.haier.hailian.contract.dto.homepage.ContractListsDto;
import com.haier.hailian.contract.service.homepage.HomePageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyq 2019年12月17日 14:32:15
 */
@Api(value = "首页相关接口", tags = {"首页相关接口"})
@RestController
@Slf4j
public class HomePageController {

    @Autowired
    private HomePageService homePageService;


    @PostMapping(value = {"/contractList"})
    @ApiOperation(value = "抢入合约列表查询接口")
    public R getContractList(@RequestBody ContractListsDto contractListsDto) {
        try{
            List<ContractListRes> res = homePageService.getContractList(contractListsDto);
            return R.ok().put("data",res);
        }catch (Exception e){
            e.printStackTrace();
            return R.error("获取：" + e.getMessage());
        }
    }


    @PostMapping(value = {"/chainGroupInfo"})
    @ApiOperation(value = "链群详情查询接口")
    public R getChainGroupInfo(@RequestBody ChainGroupInfoDto chainGroupInfoDto) {
        try{
            Map<String,Object> res = homePageService.getChainGroupInfo(chainGroupInfoDto);
            return R.ok().put("data",res);
        }catch (Exception e){
            e.printStackTrace();
            return R.error("获取：" + e.getMessage());
        }
    }
}