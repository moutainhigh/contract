package com.haier.hailian.contract.controller.homepage;

import com.haier.hailian.contract.dto.R;
import com.haier.hailian.contract.dto.homepage.*;
import com.haier.hailian.contract.entity.ZHrChainInfo;
import com.haier.hailian.contract.service.ZHrChainInfoService;
import com.haier.hailian.contract.service.homepage.HomePageService;
import com.haier.hailian.contract.service.impl.ZHrChainInfoServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
    @Autowired
    private ZHrChainInfoService zHrChainInfoService;


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


    @PostMapping(value = {"/contractData"})
    @ApiOperation(value = "外部获取数据接口")
    public R getContractData(@RequestBody DataInfo dataInfo) {
        try{
            List<Map<String , Object>> list = new ArrayList<>();
            List<ZHrChainInfo> chainInfoList = zHrChainInfoService.queryAll(new ZHrChainInfo());
            for(ZHrChainInfo zHrChainInfo : chainInfoList){
                dataInfo.setChainCode(zHrChainInfo.getChainCode());
                Map<String,Object> res = homePageService.getContractData(dataInfo);
                list.add(res);
            }
            return R.ok().put("data",list);
        }catch (Exception e){
            e.printStackTrace();
            return R.error("获取：" + e.getMessage());
        }
    }

    @GetMapping(value = {"/chainData"})
    @ApiOperation(value = "外部获取链群组织数据接口")
    public R getChainData() {
        try{
            List<ChainDataInfo> res = homePageService.getChainData() ;
            return R.ok().put("data",res);
        }catch (Exception e){
            e.printStackTrace();
            return R.error("获取：" + e.getMessage());
        }
    }
}
