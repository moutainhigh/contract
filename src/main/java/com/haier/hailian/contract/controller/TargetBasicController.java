package com.haier.hailian.contract.controller;


import com.haier.hailian.contract.dto.R;
import com.haier.hailian.contract.dto.TargetBasicInfo;
import com.haier.hailian.contract.entity.SysXiaoweiEhr;
import com.haier.hailian.contract.entity.TargetBasic;
import com.haier.hailian.contract.dto.QueryBottomDTO;
import com.haier.hailian.contract.entity.XiaoweiEhr;
import com.haier.hailian.contract.service.TargetBasicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 01431594
 * @since 2019-12-18
 */
@RestController
@RequestMapping("/targetBasic")
@Api(value = "目标底线接口", tags = "目标底线接口")
public class TargetBasicController {

    @Autowired
    private TargetBasicService targetBasicService;

    @PostMapping(value = {"/selectBottom"})
    @ApiOperation(value = "链群主举单查询目标底线")
    public R selectBottom(@RequestBody QueryBottomDTO dto) {
        List<TargetBasic> list = targetBasicService.selectBottom(dto);
        return R.ok().put("data",list);
    }


    @PostMapping(value = {"/selectContractsFirstTarget"})
    @ApiOperation(value = "查询一级单")
    public R selectContractsFirstTarget(@RequestBody QueryBottomDTO dto) {
        List<TargetBasic> list = targetBasicService.selectContractsFirstTarget(dto);
        return R.ok().put("data",list);
    }


    @PostMapping(value = {"/selectContractsSecondTarget"})
    @ApiOperation(value = "查询二级单")
    public R selectContractsSecondTarget(@RequestBody QueryBottomDTO dto) {
        List<TargetBasic> list = targetBasicService.selectContractsSecondTarget(dto);
        return R.ok().put("data",list);
    }


    @PostMapping(value = {"/updateContractsTarget"})
    @ApiOperation(value = "更新一、二级单")
    public R updateContractsTarget(@RequestBody List<TargetBasic> targetBasicList) {
        int num = targetBasicService.updateContractsTarget(targetBasicList);
        return R.ok().put("data",num);
    }


    @PostMapping(value = {"/insertContractsTarget"})
    @ApiOperation(value = "插入一、二级单")
    public R insertContractsTarget(@RequestBody List<TargetBasic> targetBasicList) {
        int num = targetBasicService.insertContractsTarget(targetBasicList);
        return R.ok().put("data",num);
    }


    @PostMapping(value = {"/selectContractsTarget"})
    @ApiOperation(value = "查询一级以及二级单")
    public R selectContractsTarget(@RequestBody QueryBottomDTO dto) {
        List<TargetBasicInfo> list = targetBasicService.selectContractsTarget(dto);
        return R.ok().put("data",list);
    }

    @PostMapping(value = {"/deleteContractsTarget"})
    @ApiOperation(value = "删除一级单以及二级单（支持只删除2级单）")
    public R deleteContractsTarget(@RequestBody Integer id) {
        int num = targetBasicService.deleteContractsTarget(id);
        return R.ok().put("data",num);
    }


    @PostMapping(value = {"/selectXwAll"})
    @ApiOperation(value = "查询小微列表")
    public R selectXwAll(@RequestBody XiaoweiEhr xiaoweiEhr) {
        List<SysXiaoweiEhr> list = targetBasicService.selectXwAll(xiaoweiEhr);
        return R.ok().put("data",list);
    }


}

