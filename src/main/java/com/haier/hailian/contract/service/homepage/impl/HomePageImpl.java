package com.haier.hailian.contract.service.homepage.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haier.hailian.contract.dao.*;
import com.haier.hailian.contract.dto.homepage.ChainGroupInfoDto;
import com.haier.hailian.contract.dto.homepage.ContractListRes;
import com.haier.hailian.contract.dto.homepage.ContractListsDto;
import com.haier.hailian.contract.entity.XChainInfo;
import com.haier.hailian.contract.entity.ZContracts;
import com.haier.hailian.contract.entity.ZContractsFactor;
import com.haier.hailian.contract.service.homepage.HomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class HomePageImpl implements HomePageService {


    @Autowired
    private ZGrabContractsDao zGrabContractsDao;
    @Autowired
    private XXwActualDao xXwActualDao;
    @Autowired
    private ZContractsDao zContractsDao;
    @Autowired
    private ZContractsFactorDao zContractsFactorDao;
    @Autowired
    private XChainInfoDao xChainInfoDao;

    /**
     * 获取已抢入合约列表查询接口
     * @param contractListsDto
     * @return
     */
    @Override
    public List<ContractListRes> getContractList(ContractListsDto contractListsDto) {

        // 获取当前登录人小微
        //String xWcode = sysEmployeeEhrDao.selectXwCode(contractListsDto.getEmpSN());


        // 1. z_grab_contracts 获取抢单信息  parent_id 合约id
        // 2. 通过合约id 去 z_gambling_contracts 获取链群编码  chain_code
        // 3. 通过chain_code 去 x_chain_info 获取 链群 和 小微信息
        // 4. 通过合约id 去 z_gambling_contracts_product_index 获取对应月份预计
        // 5. 实际 - 通过x_chain_info表中的 xwcode ，去  x_xw_actual计算实际

        //contractListsDto.setEmpSN(xWcode);

        List<ContractListRes> list = zGrabContractsDao.getContractList(contractListsDto);
        list.add(new ContractListRes());
        return list;
    }


    /**
     * 获取链群详情
     * @param chainGroupInfoDto
     * @return
     */
    @Override
    public Map<String, Object> getChainGroupInfo(ChainGroupInfoDto chainGroupInfoDto) {
        Map<String, Object> map = new HashMap<>();
        // 链群等级  现在规则没出来  卡住了
        map.put("chainGroupRank","暂时不知道咋算~");
        // 链群的增值分享    实际利润 - 举单利润（链群）
        // 链群实际利润
        BigDecimal chainFact = xXwActualDao.getChainFact(chainGroupInfoDto);
        // 链群目标利润 TODO
        BigDecimal chainEstimate;

        map.put("chainShare","");
        // 节点、人的增值分享   实际利润 - 举单利润（*百分比  个人）
        map.put("nodeShare","");
        // 吸引力预计
        map.put("estimate","");
        // 吸引力实际
        map.put("fact","");
        return map;
    }


    /**
     * 根据链群编码获取数据
     * @param chainGroupInfoDto
     * @return
     */
    @Override
    public Map<String, Object> getContractData(ChainGroupInfoDto chainGroupInfoDto) {
        Map<String, Object> map = new HashMap<>();

        List<ZContracts> contractsList = zContractsDao.selectList(new QueryWrapper<ZContracts>().eq("chain_code"  , chainGroupInfoDto.getChainCode()));
        // 合约等信息
        map.put("contractsList" , contractsList);

        // 获取合约id
        int contractsId = zContractsDao.selectList(new QueryWrapper<ZContracts>().eq("chain_code"  , chainGroupInfoDto.getChainCode()).eq("contract_type" , "30")).get(0).getParentId();
        List<ZContractsFactor> factorList = zContractsFactorDao.selectList(new QueryWrapper<ZContractsFactor>().eq("contract_id" , contractsId));
        // 目标 金额 等信息
        map.put("factorList" , factorList);


        XChainInfo xChainInfo = new XChainInfo();
        xChainInfo.setLqCode(chainGroupInfoDto.getChainCode());
        List<XChainInfo> chainInfoList = xChainInfoDao.queryAll(xChainInfo);

        // 链群 组织等信息
        map.put("chainInfoList" , chainInfoList);

        return map;
    }
}
