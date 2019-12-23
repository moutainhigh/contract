package com.haier.hailian.contract.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haier.hailian.contract.entity.ZNodeTargetPercentInfo;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (ZNodeTargetPercentInfo)表数据库访问层
 *
 * @author makejava
 * @since 2019-12-19 17:29:03
 */
public interface ZNodeTargetPercentInfoDao extends BaseMapper<ZNodeTargetPercentInfo> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ZNodeTargetPercentInfo queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ZNodeTargetPercentInfo> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param zNodeTargetPercentInfo 实例对象
     * @return 对象列表
     */
    List<ZNodeTargetPercentInfo> queryAll(ZNodeTargetPercentInfo zNodeTargetPercentInfo);

    /**
     * 新增数据
     *
     * @param zNodeTargetPercentInfo 实例对象
     * @return 影响行数
     */
    int insert(ZNodeTargetPercentInfo zNodeTargetPercentInfo);

    /**
     * 修改数据
     *
     * @param zNodeTargetPercentInfo 实例对象
     * @return 影响行数
     */
    int update(ZNodeTargetPercentInfo zNodeTargetPercentInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     *  获取分享比例和节点信息
     * @param list
     * @param periodCode
     * @return
     */
    List<ZNodeTargetPercentInfo> queryByKeyWorld(@Param("list") List<String> list,@Param("periodCode")String periodCode);

    int insertBatch(@Param("list") List<ZNodeTargetPercentInfo> list);

}