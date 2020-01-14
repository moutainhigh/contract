package com.haier.hailian.contract.dao;

import com.haier.hailian.contract.entity.ZProductChain;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 01431594
 * @since 2020-01-08
 */
public interface ZProductChainDao extends BaseMapper<ZProductChain> {

    List<ZProductChain> selectSeriesByChainCode(String chainCode);
}
