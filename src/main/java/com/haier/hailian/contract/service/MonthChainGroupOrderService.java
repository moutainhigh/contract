package com.haier.hailian.contract.service;

import com.haier.hailian.contract.dto.grab.TyMasterGrabQueryDto;
import com.haier.hailian.contract.entity.MeshGrabEntity;
import com.haier.hailian.contract.entity.MonthChainGroupOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 19012964
 * @since 2019-12-17
 */
public interface MonthChainGroupOrderService extends IService<MonthChainGroupOrder> {
    /**
     * 根据合约和产品结构获取产品编码
     * @param contractId
     * @param productStru
     * @param yearMonth
     * @return
     */
    List<String> getProductByContract(Integer contractId,String productStru,
                                      List<String> yearMonth);

    List<MeshGrabEntity> queryMeshGrabIncome(TyMasterGrabQueryDto queryDto);

    /**
     * 查询网格E2E抢单收入
     * @param queryDto
     * @return
     */
    List<MeshGrabEntity> queryMeshE2EIncome(TyMasterGrabQueryDto queryDto);

    List<MeshGrabEntity> sumStruMeshGrabIncome(TyMasterGrabQueryDto queryDto);
}
