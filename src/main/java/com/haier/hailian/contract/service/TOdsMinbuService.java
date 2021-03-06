package com.haier.hailian.contract.service;

import com.haier.hailian.contract.entity.TOdsMinbu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 19012964
 * @since 2019-12-28
 */
public interface TOdsMinbuService extends IService<TOdsMinbu> {
    List<TOdsMinbu> queryMinbuByEmp(String empSn);
    Boolean isChainMaster(String empSn);
}
