package com.haier.hailian.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.haier.hailian.contract.dao.*;
import com.haier.hailian.contract.dto.*;
import com.haier.hailian.contract.entity.*;
import com.haier.hailian.contract.service.ZContractsService;
import com.haier.hailian.contract.service.ZGamblingContractsService;
import com.haier.hailian.contract.service.ZHrChainInfoService;
import com.haier.hailian.contract.util.Constant;
import com.haier.hailian.contract.util.DateFormatUtil;
import com.haier.hailian.contract.util.ExcelUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 01431594
 * @since 2019-12-19
 */
@Service
public class ZGamblingContractsServiceImpl implements ZGamblingContractsService {

    @Autowired
    private ZContractsDao contractsDao;
    @Autowired
    private ZContractsFactorDao factorDao;
    @Autowired
    private TargetBasicDao targetBasicDao;
    @Autowired
    private ZNodeTargetPercentInfoDao nodeTargetPercentInfoDao;
    @Autowired
    private ZProductChainDao productChainDao;
    @Autowired
    private ZContractsProductDao contractsProductDao;
    @Autowired
    private ZHrChainInfoDao hrChainInfoDao;
    @Autowired
    private ZContractsXwType3Dao xwtype3Dao;
    @Autowired
    private ZContractsService contractsService;
    @Autowired
    private ZHrChainInfoService chainInfoService;

    @Override
    public MarketReturnDTO selectMarket(String chainCode) {

        MarketReturnDTO dto = new MarketReturnDTO();
        //查询42个市场小微
        List<ZNodeTargetPercentInfo> list = nodeTargetPercentInfoDao.selectList(new QueryWrapper<ZNodeTargetPercentInfo>().eq("lq_code",chainCode).isNull("share_percent").orderByAsc("node_code"));
        dto.setMarket(list);
        TargetBasic targetBasic = new TargetBasic();
        //查询链群主举单时商圈的必填目标
        targetBasic.setTargetDim("02");
        targetBasic.setJudanFlag("1");
        List<TargetBasic> list1 = targetBasicDao.selectTarget(targetBasic);
        dto.setTargetRequired(list1);
        //查询链群主举单时商圈的可选目标
        targetBasic.setJudanFlag("0");
        List<TargetBasic> list2 = targetBasicDao.selectTarget(targetBasic);
        dto.setTarget(list2);
        return dto;
    }

    @Override
    public List<ZContracts> selectContractList(QueryContractListDTO queryDTO) {
        List<ZContracts> list = contractsDao.selectContractList(queryDTO);
        return list;
    }

    @Override
    public List<ZContracts> selectMyStartContract(QueryContractListDTO queryDTO) {
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        queryDTO.setUserCode(sysUser.getEmpSn());
        queryDTO.setParentId(0);
        List<ZContracts> list = contractsDao.selectContractList(queryDTO);
        //查询还没有抢入、抢入未截止的合约
        String contractIds = contractsDao.selectContractToUpdate()+",";
        if(null != list && list.size() > 0){
            for(ZContracts zContracts:list){
                int id = zContracts.getId();
                if(contractIds.indexOf(id+",")<0){
                    zContracts.setStatus2("0");
                }else {
                    zContracts.setStatus2("1");
                }
                if(null != zContracts.getCheckTime() && zContracts.getCheckTime().after(new Date())){
                    zContracts.setStatus4("1");
                }else {
                    zContracts.setStatus4("0");
                }
                if(null != zContracts.getEndDate() && zContracts.getEndDate().after(new Date())){
                    zContracts.setStatus5("1");
                }else {
                    zContracts.setStatus5("0");
                }
            }
        }
        return list;
    }

    @Override
    public List<ZContracts> selectMyGrabContract(QueryContractListDTO queryDTO) {

        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        queryDTO.setUserCode(sysUser.getEmpSn());
        List<ZContracts> list = contractsDao.selectMyGrabContract(queryDTO);
        if(null != list && list.size() > 0){
            for(ZContracts zContracts:list){
                Date endDate = zContracts.getEndDate();
                Date joinTime = zContracts.getJoinTime();
                if(joinTime.after(new Date())){
                    zContracts.setStatus2("1");
                }else {
                    zContracts.setStatus2("0");
                }
                if(endDate.after(new Date())){
                    zContracts.setStatus3("1");
                }else {
                    zContracts.setStatus3("0");
                }
            }
        }
        return list;
    }

    @Override
    public List<ZContracts> selectToGrabContract(QueryContractListDTO dto) {
        List<ZContracts> contractsList = new ArrayList<>();
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        String userCode = sysUser.getEmpSn();
        //查询用户所属的最小作战单元
        String xwCode = "";
        if(sysUser.getMinbu() != null) xwCode = sysUser.getMinbu().getLittleXwCode();
        List<ZNodeTargetPercentInfo> chainList = nodeTargetPercentInfoDao.selectChainByLittleXwCode(xwCode);
        //查询所属链群可抢入的合约
        if(null != chainList && chainList.size()>0){
            String chainStr = "";
            for(ZNodeTargetPercentInfo percentInfo:chainList){
                chainStr += percentInfo.getLqCode()+",";
            }
            String[] chainCode = chainStr.split(",");
            dto.setStatus("0");
            dto.setChainCodeList(chainCode);
            dto.setUserCode(userCode);
            dto.setLittleXwCode(xwCode);
            //查询抢入未截止的可抢合约
            contractsList = contractsDao.selectToGrabContract(dto);
            //查询抢入已截止但是本作战单元被踢出的可抢合约
            List<ZContracts> list = contractsDao.selectKickedOutContract(dto);
            if(null == contractsList) contractsList = new ArrayList<>();
            if(null != list && list.size()>0){
                contractsList.addAll(list);
            }
        }

        return contractsList;
    }

    @Override
    public List<ContractProductDTO> selectProductSeries(QueryProductChainDTO dto) {
        //1.z_target_basic 表查询爆款产品目标
        List<TargetBasic> targetList = targetBasicDao.selectList(new QueryWrapper<TargetBasic>().eq("chain_code",dto.getChainCode()).eq("period_code",dto.getMonth()).eq("target_diff_type","002"));
        if( targetList == null || targetList.size() == 0 ) {
            targetList = targetBasicDao.selectList(new QueryWrapper<TargetBasic>().eq("target_diff_type","002"));
        }
        //2.查询链群下的爆款产品的系列和场景
        List<ZProductChain> list = productChainDao.selectSeriesByChainCode(dto.getChainCode(),dto.getMonth());
        List<ContractProductDTO> productList = new ArrayList<>();
        for(ZProductChain productChain : list){
            ContractProductDTO productDTO = new ContractProductDTO();
            productDTO.setProductSeries(productChain.getProductSeries());
            productDTO.setSceneName(productChain.getSceneName());
            List<ProductTargetDTO> productTargetList = new ArrayList<>();
            for(TargetBasic targetBasic:targetList){
                ProductTargetDTO targetDTO = new ProductTargetDTO();
                targetDTO.setTargetCode(targetBasic.getTargetCode());
                targetDTO.setTargetName(targetBasic.getTargetName());
                targetDTO.setTargetUnit(targetBasic.getTargetUnit());
                targetDTO.setQtyMonth(null);
                targetDTO.setQtyYear(null);
                productTargetList.add(targetDTO);
            }
            productDTO.setTargetList(productTargetList);
            productList.add(productDTO);
        }
        return productList;
    }

    @Override
    public GamblingContractDTO selectContractById(Integer contractId) {
        GamblingContractDTO dto = new GamblingContractDTO();
        //1.查询抢单主表数据
        ZContracts contracts = contractsDao.selectByContractId(contractId);
        dto.setContractName(contracts.getContractName());
        dto.setJoinTime(contracts.getJoinTimeStr());
        dto.setStartDate(contracts.getStartDateStr());
        dto.setEndDate(contracts.getEndDateStr());
        dto.setShareSpace(contracts.getShareSpace());
        dto.setChainCode(contracts.getChainCode());
        dto.setCheckTime(contracts.getCheckTimeStr());
        dto.setId(contractId);
        //2.查询链群目标
        List<ChainGroupTargetDTO> chainList = factorDao.selectChainFactorByContractId(contractId);
        dto.setChainGroupTargetList(chainList);
        //3.查询42市场的目标
        List<ZContractsFactor> maketList = factorDao.selectList(new QueryWrapper<ZContractsFactor>().eq("contract_id",contractId).isNotNull("region_code").orderByAsc("mesh_code").orderByAsc("factor_code"));
        List<MarketTargetDTO> marketTargetList = new ArrayList<>();
        if(null != maketList && maketList.size()>0){
            MarketTargetDTO marketTargetDTO = new MarketTargetDTO();
            List<MarketTargetDTO2> targetList = new ArrayList<>();
            String meshCode = "";
            for(int i=0;i<maketList.size();i++){
                ZContractsFactor zContractsFactor = maketList.get(i);
                if(i==0){
                    meshCode = zContractsFactor.getMeshCode();
                }
                if(meshCode.equals(zContractsFactor.getMeshCode())){
                    MarketTargetDTO2 marketTargetDTO2 = new MarketTargetDTO2();
                    marketTargetDTO2.setTargetCode(zContractsFactor.getFactorCode());
                    marketTargetDTO2.setTargetName(zContractsFactor.getFactorName());
                    marketTargetDTO2.setTargetUnit(zContractsFactor.getFactorUnit());
                    marketTargetDTO2.setTargetValue(zContractsFactor.getFactorValue());
                    marketTargetDTO2.setIsLqTarget(zContractsFactor.getIsLqTarget());
                    targetList.add(marketTargetDTO2);
                    marketTargetDTO.setXwCode(zContractsFactor.getRegionCode());
                    marketTargetDTO.setXwName(zContractsFactor.getRegionName());
                    marketTargetDTO.setNodeCode(zContractsFactor.getMeshCode());
                    marketTargetDTO.setNodeName(zContractsFactor.getMeshName());
                }else {
                    marketTargetDTO.setTargetList(targetList);
                    marketTargetList.add(marketTargetDTO);
                    marketTargetDTO = new MarketTargetDTO();
                    targetList = new ArrayList<>();
                    meshCode = zContractsFactor.getMeshCode();
                    MarketTargetDTO2 marketTargetDTO2 = new MarketTargetDTO2();
                    marketTargetDTO2.setTargetCode(zContractsFactor.getFactorCode());
                    marketTargetDTO2.setTargetName(zContractsFactor.getFactorName());
                    marketTargetDTO2.setTargetUnit(zContractsFactor.getFactorUnit());
                    marketTargetDTO2.setTargetValue(zContractsFactor.getFactorValue());
                    marketTargetDTO2.setIsLqTarget(zContractsFactor.getIsLqTarget());
                    targetList.add(marketTargetDTO2);
                    marketTargetDTO.setXwCode(zContractsFactor.getRegionCode());
                    marketTargetDTO.setXwName(zContractsFactor.getRegionName());
                    marketTargetDTO.setNodeCode(zContractsFactor.getMeshCode());
                    marketTargetDTO.setNodeName(zContractsFactor.getMeshName());
                }
            }
            marketTargetDTO.setTargetList(targetList);
            marketTargetList.add(marketTargetDTO);
        }
        dto.setMarketTargetList(marketTargetList);
        //4.查询产品目标
        List<ContractProductDTO> productDTOs = contractsProductDao.selectProductByContractId(contractId);
        dto.setProductList(productDTOs);
        //5.查询主链群的资源类型的最小作战单元个数
        String chainCode = contracts.getChainCode();
        List<ContractXwType3DTO> xwType3DTOs = xwtype3Dao.selectXwType3ByContractId(chainCode,contractId);
        dto.setXwType3List(xwType3DTOs);
        //5.查询子链群的合约
        List<ZContracts> children = contractsDao.selectList(new QueryWrapper<ZContracts>().eq("parent_id",contractId));
        if(null != children && children.size()>0){
            List<ChildTargetDTO> childrenList = new ArrayList<>();
            for(ZContracts child:children){
                ChildTargetDTO childTargetDTO = new ChildTargetDTO();
                Integer childId = child.getId();
                childTargetDTO.setId(childId);
                childTargetDTO.setShareSpace(child.getShareSpace());
                childTargetDTO.setChildChainCode(child.getChainCode());
                childTargetDTO.setChildChainName(child.getContractName());
                //6.查询子链群的链群目标
                List<ChainGroupTargetDTO> childTarget = factorDao.selectChainFactorByContractId(childId);
                childTargetDTO.setChildTargetList(childTarget);
                //7.查询子链群的市场目标
                List<ZContractsFactor> childMarkets = factorDao.selectList(new QueryWrapper<ZContractsFactor>().eq("contract_id",childId).isNotNull("region_code").orderByAsc("region_code").orderByAsc("factor_code"));
                List<MarketTargetDTO> childMarketList = new ArrayList<>();
                if(null != childMarkets && childMarkets.size()>0){
                    String regionCode = "";
                    MarketTargetDTO childMarket = new MarketTargetDTO();
                    List<MarketTargetDTO2> childMarket2 = new ArrayList<>();
                    for(int i=0;i<childMarkets.size();i++){
                        ZContractsFactor zContractsFactor = childMarkets.get(i);
                        if(i==0){
                            regionCode = zContractsFactor.getRegionCode();
                        }
                        if(regionCode.equals(zContractsFactor.getRegionCode())){
                            MarketTargetDTO2 marketTargetDTO2 = new MarketTargetDTO2();
                            marketTargetDTO2.setIsLqTarget(zContractsFactor.getIsLqTarget());
                            marketTargetDTO2.setTargetCode(zContractsFactor.getFactorCode());
                            marketTargetDTO2.setTargetName(zContractsFactor.getFactorName());
                            marketTargetDTO2.setTargetUnit(zContractsFactor.getFactorUnit());
                            marketTargetDTO2.setTargetValue(zContractsFactor.getFactorValue());
                            childMarket2.add(marketTargetDTO2);
                            childMarket.setXwCode(zContractsFactor.getRegionCode());
                            childMarket.setXwName(zContractsFactor.getRegionName());
                            childMarket.setNodeCode(zContractsFactor.getMeshCode());
                            childMarket.setNodeName(zContractsFactor.getMeshName());
                        }else {
                            childMarket.setTargetList(childMarket2);
                            childMarketList.add(childMarket);
                            childMarket = new MarketTargetDTO();
                            childMarket2 = new ArrayList<>();
                            regionCode = zContractsFactor.getRegionCode();
                            MarketTargetDTO2 marketTargetDTO2 = new MarketTargetDTO2();
                            marketTargetDTO2.setIsLqTarget(zContractsFactor.getIsLqTarget());
                            marketTargetDTO2.setTargetCode(zContractsFactor.getFactorCode());
                            marketTargetDTO2.setTargetName(zContractsFactor.getFactorName());
                            marketTargetDTO2.setTargetUnit(zContractsFactor.getFactorUnit());
                            marketTargetDTO2.setTargetValue(zContractsFactor.getFactorValue());
                            childMarket2.add(marketTargetDTO2);
                            childMarket.setXwCode(zContractsFactor.getRegionCode());
                            childMarket.setXwName(zContractsFactor.getRegionName());
                            childMarket.setNodeCode(zContractsFactor.getMeshCode());
                            childMarket.setNodeName(zContractsFactor.getMeshName());
                        }
                    }
                    childMarket.setTargetList(childMarket2);
                    childMarketList.add(childMarket);
                }
                childTargetDTO.setChildMarketList(childMarketList);
                //8.查询子链群的产品目标
                List<ContractProductDTO> childProductList = contractsProductDao.selectProductByContractId(childId);
                childTargetDTO.setChildProductList(childProductList);
                //9.查询主链群的资源类型的最小作战单元个数
                List<ContractXwType3DTO> childXwType3List = xwtype3Dao.selectXwType3ByContractId(child.getChainCode(),childId);
                childTargetDTO.setChildXwType3List(childXwType3List);
                childrenList.add(childTargetDTO);
            }
            dto.setChildren(childrenList);
        }

        return dto;
    }

    @Override
    public void exportMarket(String chainCode,HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<ZNodeTargetPercentInfo> list = nodeTargetPercentInfoDao.selectList(new QueryWrapper<ZNodeTargetPercentInfo>().eq("lq_code",chainCode).isNull("share_percent").orderByAsc("node_code"));
        Workbook workbook = new HSSFWorkbook();
        List<TargetBasic> targetBasicList = chainInfoService.getTYNodeTargetList(chainCode);
        if (targetBasicList == null || targetBasicList.size() == 0) {
            throw new RException("请先维护体验节点的目标",Constant.CODE_VALIDFAIL);
        }
        List<ExcelUtil.CellHeadField> titleList = new ArrayList<>();
        titleList.add(new ExcelUtil.CellHeadField("中心编码", "xwCode"));
        titleList.add(new ExcelUtil.CellHeadField("中心名称", "xwName"));
        titleList.add(new ExcelUtil.CellHeadField("最小作战单元编码", "nodeCode"));
        titleList.add(new ExcelUtil.CellHeadField("最小作战单元名称", "nodeName"));
        for(TargetBasic targetBasic : targetBasicList){
            ExcelUtil.CellHeadField headField = new ExcelUtil.CellHeadField(targetBasic.getTargetName()+"("+targetBasic.getTargetUnit()+")", "");
            titleList.add(headField);
        }
        ExcelUtil.CellHeadField[] headFields = new ExcelUtil.CellHeadField[titleList.size()];
        ExcelUtil.buildSheet(workbook, "中心", list, titleList.toArray(headFields));

        ByteArrayOutputStream bot = new ByteArrayOutputStream();
        workbook.write(bot);
        ExcelUtil.export(request,response,workbook,"中心.xls");

    }

    @Override
    public List<MarketTargetDTO> getMarketTargetListByExcel(InputStream inputStream, String fileName,String chainCode) throws Exception{

        List<MarketTargetDTO> list = new ArrayList<>();
        //创建Excel工作薄
        Workbook work = this.getWorkbook(inputStream, fileName);
        if (null == work) {
            throw new RException("Excle工作簿为空",Constant.CODE_VALIDFAIL);
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        sheet = work.getSheetAt(0);
        if (sheet == null) {
            throw new RException("Excle工作簿为空",Constant.CODE_VALIDFAIL);
        }
        List<TargetBasic> targetBasicList = chainInfoService.getTYNodeTargetList(chainCode);
        if (targetBasicList == null || targetBasicList.size() == 0) {
            throw new RException("请先维护体验节点的目标",Constant.CODE_VALIDFAIL);
        }
        List<String> titleList = new ArrayList<>();
        titleList.add("中心编码");
        titleList.add("中心名称");
        titleList.add("最小作战单元编码");
        titleList.add("最小作战单元名称");
        for(TargetBasic targetBasic : targetBasicList){
            titleList.add(targetBasic.getTargetName()+"("+targetBasic.getTargetUnit()+")");
        }
        for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
             row = sheet.getRow(j);
            if (row == null ) {
                continue;
            }
            if( j==0 ){
                if(row.getLastCellNum() != titleList.size()){
                    throw new RException("请先下载模板，再上传",Constant.CODE_VALIDFAIL);
                }
                for(int i = 0 ; i<titleList.size() ; i++ ){
                    String title = titleList.get(i);
                    if(!title.equals(row.getCell(i).getStringCellValue())){
                        throw new RException("请先下载模板，再上传",Constant.CODE_VALIDFAIL);
                    }
                }
            }else {
                MarketTargetDTO marketTargetDTO = new MarketTargetDTO();
                List<MarketTargetDTO2> dto2List = new ArrayList<>();
                int lastCellNum = row.getLastCellNum();
                if( lastCellNum > titleList.size() ) lastCellNum = titleList.size();
                for (int y = row.getFirstCellNum(); y < lastCellNum; y++) {
                    cell = row.getCell(y);
                    if(cell != null){
                        if(y==0) marketTargetDTO.setXwCode(cell.getStringCellValue());
                        if(y==1) marketTargetDTO.setXwName(cell.getStringCellValue());
                        if(y==2) marketTargetDTO.setNodeCode(cell.getStringCellValue());
                        if(y==3) marketTargetDTO.setNodeName(cell.getStringCellValue());
                        if(y>3){
                            if(cell.getCellTypeEnum().equals(CellType.STRING)){
                                throw new RException("第"+(j+1)+"行第"+(y+1)+"列，请填写数字，不需要单位",Constant.CODE_VALIDFAIL);
                            }else {
                                MarketTargetDTO2 marketTargetDTO2 = new MarketTargetDTO2();
                                BigDecimal targetValue = BigDecimal.valueOf(cell.getNumericCellValue());
                                marketTargetDTO2.setTargetValue(targetValue.setScale(2,BigDecimal.ROUND_HALF_UP)+"");
                                marketTargetDTO2.setTargetCode(targetBasicList.get(y-4).getTargetCode());
                                dto2List.add(marketTargetDTO2);
                            }
                        }
                    }
                }
                marketTargetDTO.setTargetList(dto2List);
                list.add(marketTargetDTO);
            }
        }

        work.close();
        return list;
    }

    @Override
    public List<ZContracts> selectHomePageContract(QueryContractListDTO2 queryDTO) {
        List<ZContracts> list = new ArrayList<>();
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        queryDTO.setUserCode(sysUser.getEmpSn());
        List<ZContracts> list2 = contractsDao.selectHomePageContract(queryDTO);
        //查当前月份的时候，链群主假数据
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMM");
        //1.查询链群主的链群
        List<ZHrChainInfo> chainList = hrChainInfoDao.selectList(new QueryWrapper<ZHrChainInfo>().eq("master_code",sysUser.getEmpSn()));
        //2.查询每个链群是否举了下个月的单，未举单的链群产生假数据
        if(null != chainList && chainList.size() > 0){
            for(ZHrChainInfo chainInfo : chainList){
                if(null != chainInfo.getParentCode() && !"0".equals(chainInfo.getParentCode()) && !"".equals(chainInfo.getParentCode())) continue;
                QueryContractListDTO dto = new QueryContractListDTO();
                dto.setChainCode(chainInfo.getChainCode());
                int next = DateFormatUtil.getMonthOfDate(new Date())+1;
                int year = DateFormatUtil.getYearOfDate(new Date());
                String nextMonth = "";
                nextMonth = next<10?year+"0"+next:year+""+next;
                dto.setNextMonth(nextMonth);
                List<ZContracts> contractsList = contractsDao.selectContractList(dto);
                if(null == contractsList || contractsList.size()==0){
                    ZContracts zContracts = new ZContracts();
                    zContracts.setContractName(chainInfo.getChainName());
                    //从z_waring_period_config表中查询举单开始日期
                    Date begin = contractsDao.selectGamnlingBeginDate(chainInfo.getChainCode());
                    int day = 0;
                    if(begin != null){
                        day = DateFormatUtil.getDAYOfDate(begin);
                    }else {
                        day = 20;
                    }
                    if(DateFormatUtil.getDAYOfDate(new Date())<day){
                        zContracts.setStatus("close");
                    }else{
                        zContracts.setStatus("open");
                    }
                    list.add(zContracts);
                }
            }
        }
        list.addAll(list2);
        return list;
    }

    @Override
    public void exportProductSeries(String chainCode, HttpServletRequest request, HttpServletResponse response) throws IOException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMM");
        List<ZProductChain> list = productChainDao.selectSeriesByChainCode(chainCode,sf.format(new Date()));

        Workbook workbook = new HSSFWorkbook();
        ExcelUtil.buildSheet(workbook, "产品系列", list, TEMPLATE_TITLE_PRODUCT);
        ByteArrayOutputStream bot = new ByteArrayOutputStream();
        workbook.write(bot);
        ExcelUtil.export(request,response,workbook,"产品系列.xls");
    }

    @Override
    public List<ContractProductDTO> getProductSeriesListByExcel(InputStream inputStream, String fileName) throws Exception{
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        List list = new ArrayList<>();
        //创建Excel工作薄
        Workbook work = this.getWorkbook(inputStream, fileName);
        if (null == work) {
            throw new RException("Excle工作簿为空",Constant.CODE_VALIDFAIL);
        }
        sheet = work.getSheetAt(0);
        if (sheet == null) {
            throw new RException("Excle工作簿为空",Constant.CODE_VALIDFAIL);
        }
        for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
            row = sheet.getRow(j);
            if (row == null ) {
                continue;
            }
            if(row.getFirstCellNum() == j){
                String title1 = row.getCell(0)==null?"":row.getCell(0).getStringCellValue();
                String title2 = row.getCell(1)==null?"":row.getCell(1).getStringCellValue();
                String title3 = row.getCell(2)==null?"":row.getCell(2).getStringCellValue();
                if("系列名称".equals(title1)&&"年度销量(台)".equals(title2)&&"月度销量(台)".equals(title3)){
                    continue;
                }else {
                    throw new RException("请先下载模板，再上传",Constant.CODE_VALIDFAIL);
                }
            }
            ContractProductDTO dto = new ContractProductDTO();
            for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                cell = row.getCell(y);
                if(cell != null){
                    if(y==0) dto.setProductSeries(cell.getStringCellValue());
                    if(y==1){
                        if(cell.getCellTypeEnum().equals(CellType.STRING)){
                            throw new RException("第"+(j+1)+"行第"+(y+1)+"列，请填写数字，不需要单位",Constant.CODE_VALIDFAIL);
                        }else {
                            BigDecimal qty = BigDecimal.valueOf(cell.getNumericCellValue());
                            if(qty.compareTo(BigDecimal.ZERO)==-1 || qty.compareTo(new BigDecimal(qty.intValue()))==1){
                                throw new RException("第" + (j + 1) + "行第" + (y + 1) + "列，销量必须为大于等于零的整数", Constant.CODE_VALIDFAIL);
                            }
                            //dto.setQtyYear(Integer.valueOf((int) cell.getNumericCellValue()));
                        }
                    }
                    if(y==2){
                        if(cell.getCellTypeEnum().equals(CellType.STRING)){
                            throw new RException("第"+(j+1)+"行第"+(y+1)+"列，请填写数字，不需要单位",Constant.CODE_VALIDFAIL);
                        }else {
                            BigDecimal qty = BigDecimal.valueOf(cell.getNumericCellValue());
                            if(qty.compareTo(BigDecimal.ZERO)==-1 || qty.compareTo(new BigDecimal(qty.intValue()))==1){
                                throw new RException("第" + (j + 1) + "行第" + (y + 1) + "列，销量必须为大于零的整数", Constant.CODE_VALIDFAIL);
                            }
                            //dto.setQtyMonth(Integer.valueOf((int) cell.getNumericCellValue()));
                        }
                    }
                }
            }
            list.add(dto);
        }

        work.close();
        return list;
    }

    @Override
    public void saveGamblingNew(SaveGamblingContractDTO dto) throws Exception{


        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        TOdsMinbu currentUser = sysUser.getMinbu();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMM");
        //1.保存链群主抢单信息到合同主表
        ZContracts contracts = new ZContracts();
        if(null == dto.getId() || dto.getId() == 0){
            //ID 为空时为新增
            BeanUtils.copyProperties(dto,contracts);
            if(dto.getStartDate() != null) contracts.setStartDate(sf.parse(dto.getStartDate()));
            if(dto.getEndDate() != null) contracts.setEndDate(sf.parse(dto.getEndDate()));
            if(dto.getJoinTime() != null) contracts.setJoinTime(sf.parse(dto.getJoinTime()));
            if(dto.getCheckTime() != null) contracts.setCheckTime(sf.parse(dto.getCheckTime()));
            contracts.setContractType("10");
            if("1".equals(dto.getIsDraft())){
                contracts.setStatus("9");
            }else{
                contracts.setStatus("0");
            }
            contracts.setCreateName(sysUser.getEmpName());
            contracts.setCreateCode(sysUser.getEmpSn());
            contracts.setCreateTime(new Date());
            contracts.setXiaoweiCode(currentUser.getXwCode());
            contracts.setOrgName(currentUser.getLittleXwName());
            contracts.setOrgCode(currentUser.getLittleXwCode());
            contracts.setContractName(dto.getContractName());
            contractsDao.insert(contracts);
        }else{
            //ID 不为0时，为修改
            contracts = contractsDao.selectByContractId(dto.getId());
            contracts.setShareSpace(dto.getShareSpace());
            if(dto.getStartDate() != null) contracts.setStartDate(sf.parse(dto.getStartDate()));
            if(dto.getEndDate() != null) contracts.setEndDate(sf.parse(dto.getEndDate()));
            if(dto.getJoinTime() != null) contracts.setJoinTime(sf.parse(dto.getJoinTime()));
            if(dto.getCheckTime() != null) contracts.setCheckTime(sf.parse(dto.getCheckTime()));
            if("1".equals(dto.getIsDraft())){
                contracts.setStatus("9");
            }else{
                contracts.setStatus("0");
            }
            contractsDao.updateById(contracts);
            //修改时删除原有目标
            factorDao.delete(new QueryWrapper<ZContractsFactor>().eq("contract_id",dto.getId()));
            contractsProductDao.delete(new QueryWrapper<ZContractsProduct>().eq("contract_id",dto.getId()));
            xwtype3Dao.delete(new QueryWrapper<ZContractsXwType3>().eq("contract_id",dto.getId()));

        }
        //2.保存链群目标到目标表
        List<ChainGroupTargetDTO> chainGroupTargetList = dto.getChainGroupTargetList();
        for(ChainGroupTargetDTO chainGroupTarget:chainGroupTargetList){
            if(!"1".equals(dto.getIsDraft()) && null==chainGroupTarget.getGrab()) continue;
            if(null == chainGroupTarget.getE2E() || null == chainGroupTarget.getBottom()) throw new RException("链群的底线和E2E目标未维护，无法举单",Constant.CODE_VALIDFAIL);
            ZContractsFactor factor1 = new ZContractsFactor();
            factor1.setContractId(contracts.getId());
            factor1.setFactorValue(chainGroupTarget.getBottom());
            factor1.setFactorCode(chainGroupTarget.getTargetCode());
            factor1.setFactorName(chainGroupTarget.getTargetName());
            factor1.setFactorType(Constant.FactorType.Bottom.getValue());
            factor1.setFactorUnit(chainGroupTarget.getTargetUnit());
            factor1.setIsLqTarget(chainGroupTarget.getIsLqTarget());
            factorDao.insert(factor1);
            factor1.setId(null);
            factor1.setFactorValue(chainGroupTarget.getE2E());
            factor1.setFactorType(Constant.FactorType.E2E.getValue());
            factor1.setIsLqTarget(chainGroupTarget.getIsLqTarget());
            factorDao.insert(factor1);
            factor1.setId(null);
            factor1.setFactorValue(chainGroupTarget.getGrab());
            factor1.setFactorType(Constant.FactorType.Grab.getValue());
            factor1.setIsLqTarget(chainGroupTarget.getIsLqTarget());
            factorDao.insert(factor1);
        }
        //3.保存主链群市场目标到目标表
        List<MarketTargetDTO> marketTargetList = dto.getMarketTargetList();
        if(null != marketTargetList){
            for (MarketTargetDTO marketTarget : marketTargetList){
                List<MarketTargetDTO2> dto2List = marketTarget.getTargetList();
                for(MarketTargetDTO2 dto2:dto2List){
                    ZContractsFactor factor = new ZContractsFactor();
                    factor.setContractId(contracts.getId());
                    factor.setFactorValue(dto2.getTargetValue());
                    factor.setFactorCode(dto2.getTargetCode());
                    factor.setFactorName(dto2.getTargetName());
                    factor.setFactorType(Constant.FactorType.Grab.getValue());
                    factor.setFactorUnit(dto2.getTargetUnit());
                    factor.setRegionCode(marketTarget.getXwCode());
                    factor.setRegionName(marketTarget.getXwName());
                    factor.setMeshName(marketTarget.getNodeName());
                    factor.setMeshCode(marketTarget.getNodeCode());
                    factor.setIsLqTarget(dto2.getIsLqTarget());
                    factorDao.insert(factor);
                }
            }
        }

        //4 保存主链群产品系列目标到合约产品表
        List<ContractProductDTO> productList = dto.getProductList();
        if(null != productList){
            for (ContractProductDTO productDTO : productList){
                ZContractsProduct contractsProduct = new ZContractsProduct();
                contractsProduct.setContractId(contracts.getId());
                contractsProduct.setProductSeries(productDTO.getProductSeries());
                contractsProduct.setSceneName(productDTO.getSceneName());
                List<ProductTargetDTO>  targetList = productDTO.getTargetList();
                for(ProductTargetDTO targetDTO:targetList){
                    contractsProduct.setQtyMonth(targetDTO.getQtyMonth());
                    contractsProduct.setQtyYear(targetDTO.getQtyYear());
                    contractsProduct.setTargetUnit(targetDTO.getTargetUnit());
                    contractsProduct.setTargetName(targetDTO.getTargetName());
                    contractsProduct.setTargetCode(targetDTO.getTargetCode());
                    contractsProduct.setPeriodCode(sf2.format(sf.parse(dto.getStartDate())));
                    contractsProduct.setChainCode(dto.getChainCode());
                    contractsProduct.setChainName(dto.getContractName());
                    contractsProductDao.insert(contractsProduct);
                }
            }
        }
        //5.保存主链群资源类型下的最小作战单元数量
        List<ContractXwType3DTO> xwType3List = dto.getXwType3List();
        if(null != xwType3List){
            for (ContractXwType3DTO xwType3DTO : xwType3List){
                ZContractsXwType3 xwType3 = new ZContractsXwType3();
                xwType3.setContractId(contracts.getId());
                xwType3.setNodeNumber(xwType3DTO.getInputNumber());
                xwType3.setXwType3(xwType3DTO.getXwType3());
                xwType3.setXwType3Code(xwType3DTO.getXwType3Code());
                xwtype3Dao.insert(xwType3);
            }
        }
        //6.保存子链群合约
        List<ChildTargetDTO> children = dto.getChildren();
        if(null != children && children.size()>0){
            for(ChildTargetDTO child:children){
                ZContracts childContracts = new ZContracts();
                if(null == child.getId() || child.getId() == 0){
                    //ID 为空时为新增
                    childContracts.setParentId(contracts.getId());
                    childContracts.setShareSpace(child.getShareSpace());
                    childContracts.setChainCode(child.getChildChainCode());
                    if(dto.getStartDate() != null) childContracts.setStartDate(sf.parse(dto.getStartDate()));
                    if(dto.getEndDate() != null) childContracts.setEndDate(sf.parse(dto.getEndDate()));
                    if(dto.getJoinTime() != null) childContracts.setJoinTime(sf.parse(dto.getJoinTime()));
                    if(dto.getCheckTime() != null) childContracts.setCheckTime(sf.parse(dto.getCheckTime()));
                    childContracts.setContractType("10");
                    if("1".equals(dto.getIsDraft())){
                        childContracts.setStatus("9");
                    }else{
                        childContracts.setStatus("0");
                    }
                    childContracts.setCreateName(sysUser.getEmpName());
                    childContracts.setCreateCode(sysUser.getEmpSn());
                    childContracts.setCreateTime(new Date());
                    childContracts.setXiaoweiCode(currentUser.getXwCode());
                    childContracts.setOrgName(currentUser.getLittleXwName());
                    childContracts.setOrgCode(currentUser.getLittleXwCode());
                    childContracts.setContractName(child.getChildChainName());
                    contractsDao.insert(childContracts);
                }else{
                    //ID 不为0时，为修改
                    childContracts = contractsDao.selectByContractId(child.getId());
                    if("1".equals(dto.getIsDraft())){
                        childContracts.setStatus("9");
                    }else{
                        childContracts.setStatus("0");
                    }
                    childContracts.setShareSpace(child.getShareSpace());
                    if(dto.getStartDate() != null) childContracts.setStartDate(sf.parse(dto.getStartDate()));
                    if(dto.getEndDate() != null) childContracts.setEndDate(sf.parse(dto.getEndDate()));
                    if(dto.getJoinTime() != null) childContracts.setJoinTime(sf.parse(dto.getJoinTime()));
                    if(dto.getCheckTime() != null) childContracts.setCheckTime(sf.parse(dto.getCheckTime()));
                    contractsDao.updateById(childContracts);
                    //修改时删除原有目标
                    contractsProductDao.delete(new QueryWrapper<ZContractsProduct>().eq("contract_id",child.getId()));
                    factorDao.delete(new QueryWrapper<ZContractsFactor>().eq("contract_id",child.getId()));
                    xwtype3Dao.delete(new QueryWrapper<ZContractsXwType3>().eq("contract_id",child.getId()));

                }
                //7.保存子链群的链群目标
                List<ChainGroupTargetDTO> childTargetList = child.getChildTargetList();
                for(ChainGroupTargetDTO childTarget:childTargetList){
                    if(!"1".equals(dto.getIsDraft()) && null==childTarget.getGrab()) continue;
                    if(null == childTarget.getE2E() || null == childTarget.getBottom()) throw new RException("链群的底线和E2E目标未维护，无法举单",Constant.CODE_VALIDFAIL);
                    ZContractsFactor factor1 = new ZContractsFactor();
                    factor1.setIsLqTarget(childTarget.getIsLqTarget());
                    factor1.setContractId(childContracts.getId());
                    factor1.setFactorValue(childTarget.getBottom());
                    factor1.setFactorCode(childTarget.getTargetCode());
                    factor1.setFactorName(childTarget.getTargetName());
                    factor1.setFactorType(Constant.FactorType.Bottom.getValue());
                    factor1.setFactorUnit(childTarget.getTargetUnit());
                    factorDao.insert(factor1);
                    factor1.setId(null);
                    factor1.setFactorValue(childTarget.getE2E());
                    factor1.setFactorType(Constant.FactorType.E2E.getValue());
                    factor1.setIsLqTarget(childTarget.getIsLqTarget());
                    factorDao.insert(factor1);
                    factor1.setId(null);
                    factor1.setFactorValue(childTarget.getGrab());
                    factor1.setFactorType(Constant.FactorType.Grab.getValue());
                    factor1.setIsLqTarget(childTarget.getIsLqTarget());
                    factorDao.insert(factor1);
                }
                //8.保存子链群的市场目标
                List<MarketTargetDTO> chilidMarketList = child.getChildMarketList();
                if(null != chilidMarketList){
                    for (MarketTargetDTO chilidMarket : chilidMarketList){
                        List<MarketTargetDTO2> dto2List = chilidMarket.getTargetList();
                        for(MarketTargetDTO2 dto2:dto2List){
                            ZContractsFactor factor = new ZContractsFactor();
                            factor.setIsLqTarget(dto2.getIsLqTarget());
                            factor.setContractId(childContracts.getId());
                            factor.setFactorValue(dto2.getTargetValue());
                            factor.setFactorCode(dto2.getTargetCode());
                            factor.setFactorName(dto2.getTargetName());
                            factor.setFactorType(Constant.FactorType.Grab.getValue());
                            factor.setFactorUnit(dto2.getTargetUnit());
                            factor.setRegionCode(chilidMarket.getXwCode());
                            factor.setRegionName(chilidMarket.getXwName());
                            factor.setMeshCode(chilidMarket.getNodeCode());
                            factor.setMeshName(chilidMarket.getNodeName());
                            factorDao.insert(factor);
                        }
                    }
                }
                //9.保存子链群的产品目标
                List<ContractProductDTO> childProductList = child.getChildProductList();
                if(null != childProductList){
                    for (ContractProductDTO chilidProduct : childProductList){
                        ZContractsProduct contractsProduct = new ZContractsProduct();
                        contractsProduct.setProductSeries(chilidProduct.getProductSeries());
                        contractsProduct.setContractId(childContracts.getId());
                        contractsProduct.setSceneName(chilidProduct.getSceneName());
                        List<ProductTargetDTO>  targetList = chilidProduct.getTargetList();
                        for(ProductTargetDTO targetDTO:targetList){
                            contractsProduct.setTargetUnit(targetDTO.getTargetUnit());
                            contractsProduct.setTargetName(targetDTO.getTargetName());
                            contractsProduct.setTargetCode(targetDTO.getTargetCode());
                            contractsProduct.setQtyMonth(targetDTO.getQtyMonth());
                            contractsProduct.setQtyYear(targetDTO.getQtyYear());
                            contractsProduct.setPeriodCode(sf2.format(sf.parse(dto.getStartDate())));
                            contractsProduct.setChainCode(dto.getChainCode());
                            contractsProduct.setChainName(dto.getContractName());
                            contractsProductDao.insert(contractsProduct);
                        }
                    }
                }
                //10.保存子链群资源类型下的最小作战单元数量
                List<ContractXwType3DTO> childXwType3List = child.getChildXwType3List();
                if(null != childXwType3List){
                    for (ContractXwType3DTO xwType3DTO : childXwType3List){
                        ZContractsXwType3 xwType3 = new ZContractsXwType3();
                        xwType3.setNodeNumber(xwType3DTO.getInputNumber());
                        xwType3.setContractId(childContracts.getId());
                        xwType3.setXwType3(xwType3DTO.getXwType3());
                        xwType3.setXwType3Code(xwType3DTO.getXwType3Code());
                        xwtype3Dao.insert(xwType3);
                    }
                }
            }
        }
    }

    @Override
    public List<ContractXwType3DTO> selectXwType3(String chainCode) {
        List<ContractXwType3DTO> xwType3DTOList = nodeTargetPercentInfoDao.selectXwType3ListByChainCode(chainCode);
        return xwType3DTOList;
    }

    @Override
    public List<ZContracts> selectAllGrabContract(QueryContractListDTO queryDTO) {
        //获取当前用户选择的最小作战单元
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        String xwCode = sysUser.getMinbu().getLittleXwCode();
        queryDTO.setLittleXwCode(xwCode);
        List<ZContracts> list = contractsDao.selectAllGrabContract(queryDTO);
        return list;
    }

    @Override
    public void exportGamblingContract(int contractId, HttpServletRequest request, HttpServletResponse response)  throws IOException{
        GamblingContractDTO contract = this.selectContractById(contractId);
        Workbook workbook = new HSSFWorkbook();
        CellStyle contentCellStyle = ExcelUtil.getContentCellStyle(workbook);
        CellStyle headerCellStyle = ExcelUtil.getHeaderCellStyle(workbook);
        Sheet sheet = workbook.createSheet(contract.getContractName());
        for(int i=0;i<6;i++){
            sheet.setColumnWidth(i,5000);
        }
        int rowNum = 0;
        Row row1 = sheet.createRow(0);
        rowNum++;
        Cell cell = row1.createCell(0);
        cell.setCellValue("合约名称");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(1);
        cell.setCellValue("合约开始日期");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(2);
        cell.setCellValue("合约结束日期");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(3);
        cell.setCellValue("合约截止抢入日期");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(4);
        cell.setCellValue("链群主复核截止日期");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(5);
        cell.setCellValue("链群分享空间");
        cell.setCellStyle(headerCellStyle);
        row1 = sheet.createRow(rowNum);
        rowNum++;
        cell = row1.createCell(0);
        cell.setCellValue(contract.getContractName());
        cell.setCellStyle(contentCellStyle);
        cell = row1.createCell(1);
        cell.setCellValue(contract.getStartDate());
        cell.setCellStyle(contentCellStyle);
        cell = row1.createCell(2);
        cell.setCellValue(contract.getEndDate());
        cell.setCellStyle(contentCellStyle);
        cell = row1.createCell(3);
        cell.setCellValue(contract.getJoinTime());
        cell.setCellStyle(contentCellStyle);
        cell = row1.createCell(4);
        cell.setCellValue(contract.getCheckTime());
        cell.setCellStyle(contentCellStyle);
        cell = row1.createCell(5);
        cell.setCellValue(contract.getShareSpace()+"");
        cell.setCellStyle(contentCellStyle);
        //链群目标
        List<ChainGroupTargetDTO> chainGroupTargetList = contract.getChainGroupTargetList();
        row1 = sheet.createRow(rowNum);
        rowNum++;
        cell = row1.createCell(0);
        cell.setCellValue("链群目标");
        cell.setCellStyle(headerCellStyle);
        row1 = sheet.createRow(rowNum);
        rowNum++;
        cell = row1.createCell(0);
        cell.setCellValue("目标名称");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(1);
        cell.setCellValue("底线目标");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(2);
        cell.setCellValue("E2E目标");
        cell.setCellStyle(headerCellStyle);
        cell = row1.createCell(3);
        cell.setCellValue("举单目标");
        cell.setCellStyle(headerCellStyle);
        for(ChainGroupTargetDTO chaiTargetDTO:chainGroupTargetList){
            row1 = sheet.createRow(rowNum);
            rowNum++;
            cell = row1.createCell(0);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(chaiTargetDTO.getTargetName());
            cell = row1.createCell(1);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(chaiTargetDTO.getBottom());
            cell = row1.createCell(2);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(chaiTargetDTO.getE2E());
            cell = row1.createCell(3);
            cell.setCellStyle(contentCellStyle);
            cell.setCellValue(chaiTargetDTO.getGrab());
        }
        //42中心目标
        List<MarketTargetDTO> marketTargetList = contract.getMarketTargetList();
        if(null != marketTargetList && marketTargetList.size() > 0){
            row1 = sheet.createRow(rowNum);
            rowNum++;
            cell = row1.createCell(0);
            cell.setCellValue("中心名称");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(1);
            cell.setCellValue("最小作战单元");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(2);
            cell.setCellValue("收入");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(3);
            cell.setCellValue("高端占比");
            cell.setCellStyle(headerCellStyle);
            for(MarketTargetDTO marketTargetDTO:marketTargetList){
                row1 = sheet.createRow(rowNum);
                rowNum++;
                cell = row1.createCell(0);
                cell.setCellStyle(contentCellStyle);
                cell.setCellValue(marketTargetDTO.getXwName());
                cell = row1.createCell(1);
                cell.setCellStyle(contentCellStyle);
                cell.setCellValue(marketTargetDTO.getNodeName());
                List<MarketTargetDTO2> targetList = marketTargetDTO.getTargetList();
                for(MarketTargetDTO2 targetDTO2:targetList){
                    if("T01001".equals(targetDTO2.getTargetCode())){
                        cell = row1.createCell(2);
                        cell.setCellStyle(contentCellStyle);
                        cell.setCellValue(targetDTO2.getTargetValue());
                    }
                    if("T03001".equals(targetDTO2.getTargetCode())){
                        cell = row1.createCell(3);
                        cell.setCellStyle(contentCellStyle);
                        cell.setCellValue(targetDTO2.getTargetValue());
                    }
                }
            }
        }
        //爆款目标
        List<ContractProductDTO> productList = contract.getProductList();
        if(null != productList && productList.size() > 0){
            row1 = sheet.createRow(rowNum);
            rowNum++;
            cell = row1.createCell(0);
            cell.setCellValue("场景");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(1);
            cell.setCellValue("系列名称");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(2);
            cell.setCellValue("单");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(3);
            cell.setCellValue("年度");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(4);
            cell.setCellValue("月度");
            cell.setCellStyle(headerCellStyle);
            for(ContractProductDTO productDTO:productList){
                String sceneName = productDTO.getSceneName();
                String productSeries = productDTO.getProductSeries();
                List<ProductTargetDTO> targetList = productDTO.getTargetList();
                for(ProductTargetDTO targetDTO:targetList){
                    row1 = sheet.createRow(rowNum);
                    rowNum++;
                    cell = row1.createCell(0);
                    cell.setCellStyle(contentCellStyle);
                    cell.setCellValue(sceneName);
                    cell = row1.createCell(1);
                    cell.setCellStyle(contentCellStyle);
                    cell.setCellValue(productSeries);
                    cell = row1.createCell(2);
                    cell.setCellStyle(contentCellStyle);
                    cell.setCellValue(targetDTO.getTargetName());
                    cell = row1.createCell(3);
                    cell.setCellStyle(contentCellStyle);
                    cell.setCellValue(targetDTO.getQtyYear()+"");
                    cell = row1.createCell(4);
                    cell.setCellStyle(contentCellStyle);
                    cell.setCellValue(targetDTO.getQtyMonth()+"");
                }
            }
        }
        //资源类型
        List<ContractXwType3DTO> xwType3List = contract.getXwType3List();
        if(null != xwType3List && xwType3List.size() > 0){
            row1 = sheet.createRow(rowNum);
            rowNum++;
            cell = row1.createCell(0);
            cell.setCellValue("资源类型名称");
            cell.setCellStyle(headerCellStyle);
            cell = row1.createCell(1);
            cell.setCellValue("所需最小作战单元(个)");
            cell.setCellStyle(headerCellStyle);
            for(ContractXwType3DTO xwType3DTO:xwType3List){
                row1 = sheet.createRow(rowNum);
                rowNum++;
                cell = row1.createCell(0);
                cell.setCellStyle(contentCellStyle);
                cell.setCellValue(xwType3DTO.getXwType3());
                cell = row1.createCell(1);
                cell.setCellStyle(contentCellStyle);
                cell.setCellValue(xwType3DTO.getInputNumber());
            }
        }
        //子合约
        List<ChildTargetDTO> children = contract.getChildren();
        if(null != children && children.size()>0){
           for(ChildTargetDTO childTargetDTO:children){
               sheet = workbook.createSheet(childTargetDTO.getChildChainName());
               for(int i=0;i<6;i++){
                   sheet.setColumnWidth(i,5000);
               }
               rowNum = 0;
               row1 = sheet.createRow(0);
               rowNum++;
               cell = row1.createCell(0);
               cell.setCellStyle(headerCellStyle);
               cell.setCellValue("合约名称");
               cell = row1.createCell(1);
               cell.setCellStyle(headerCellStyle);
               cell.setCellValue("链群分享空间");
               row1 = sheet.createRow(1);
               rowNum++;
               cell = row1.createCell(0);
               cell.setCellStyle(contentCellStyle);
               cell.setCellValue(childTargetDTO.getChildChainName());
               cell = row1.createCell(1);
               cell.setCellStyle(contentCellStyle);
               cell.setCellValue(childTargetDTO.getShareSpace()+"");
               //子链群的链群目标
               List<ChainGroupTargetDTO> childTargetList = childTargetDTO.getChildTargetList();
               row1 = sheet.createRow(rowNum);
               rowNum++;
               cell = row1.createCell(0);
               cell.setCellValue("链群目标");
               cell.setCellStyle(headerCellStyle);
               row1 = sheet.createRow(rowNum);
               rowNum++;
               cell = row1.createCell(0);
               cell.setCellValue("目标名称");
               cell.setCellStyle(headerCellStyle);
               cell = row1.createCell(1);
               cell.setCellValue("底线目标");
               cell.setCellStyle(headerCellStyle);
               cell = row1.createCell(2);
               cell.setCellValue("E2E目标");
               cell.setCellStyle(headerCellStyle);
               cell = row1.createCell(3);
               cell.setCellValue("举单目标");
               cell.setCellStyle(headerCellStyle);
               for(ChainGroupTargetDTO childGroupTargetDTO:childTargetList){
                   row1 = sheet.createRow(rowNum);
                   rowNum++;
                   cell = row1.createCell(0);
                   cell.setCellValue(childGroupTargetDTO.getTargetName());
                   cell.setCellStyle(contentCellStyle);
                   cell = row1.createCell(1);
                   cell.setCellStyle(contentCellStyle);
                   cell.setCellValue(childGroupTargetDTO.getBottom());
                   cell = row1.createCell(2);
                   cell.setCellStyle(contentCellStyle);
                   cell.setCellValue(childGroupTargetDTO.getE2E());
                   cell = row1.createCell(3);
                   cell.setCellStyle(contentCellStyle);
                   cell.setCellValue(childGroupTargetDTO.getGrab());
               }
               //子链群的42中心目标
               List<MarketTargetDTO> childMarketList = childTargetDTO.getChildMarketList();
               if(null != childMarketList && childMarketList.size() > 0){
                   row1 = sheet.createRow(rowNum);
                   rowNum++;
                   cell = row1.createCell(0);
                   cell.setCellStyle(headerCellStyle);
                   cell.setCellValue("中心名称");
                   cell = row1.createCell(1);
                   cell.setCellValue("最小作战单元");
                   cell.setCellStyle(headerCellStyle);
                   cell = row1.createCell(2);
                   cell.setCellValue("收入");
                   cell.setCellStyle(headerCellStyle);
                   cell = row1.createCell(3);
                   cell.setCellValue("高端占比");
                   cell.setCellStyle(headerCellStyle);
                   for(MarketTargetDTO marketTargetDTO:childMarketList){
                       row1 = sheet.createRow(rowNum);
                       rowNum++;
                       cell = row1.createCell(0);
                       cell.setCellValue(marketTargetDTO.getXwName());
                       cell.setCellStyle(contentCellStyle);
                       cell = row1.createCell(1);
                       cell.setCellStyle(contentCellStyle);
                       cell.setCellValue(marketTargetDTO.getNodeName());
                       List<MarketTargetDTO2> targetList = marketTargetDTO.getTargetList();
                       for(MarketTargetDTO2 targetDTO2:targetList){
                           if("T01001".equals(targetDTO2.getTargetCode())){
                               cell = row1.createCell(2);
                               cell.setCellValue(targetDTO2.getTargetValue());
                               cell.setCellStyle(contentCellStyle);
                           }
                           if("T03001".equals(targetDTO2.getTargetCode())){
                               cell = row1.createCell(3);
                               cell.setCellValue(targetDTO2.getTargetValue());
                               cell.setCellStyle(contentCellStyle);
                           }
                       }
                   }
               }
               //子链群的爆款目标
               List<ContractProductDTO> childProductList = childTargetDTO.getChildProductList();
               if(null != childProductList && childProductList.size() > 0){
                   row1 = sheet.createRow(rowNum);
                   rowNum++;
                   cell = row1.createCell(0);
                   cell.setCellStyle(headerCellStyle);
                   cell.setCellValue("场景");
                   cell = row1.createCell(1);
                   cell.setCellValue("系列名称");
                   cell.setCellStyle(headerCellStyle);
                   cell = row1.createCell(2);
                   cell.setCellValue("单");
                   cell.setCellStyle(headerCellStyle);
                   cell = row1.createCell(3);
                   cell.setCellValue("年度");
                   cell.setCellStyle(headerCellStyle);
                   cell = row1.createCell(4);
                   cell.setCellValue("月度");
                   cell.setCellStyle(headerCellStyle);
                   for(ContractProductDTO productDTO:childProductList){
                       String sceneName = productDTO.getSceneName();
                       String productSeries = productDTO.getProductSeries();
                       List<ProductTargetDTO> targetList = productDTO.getTargetList();
                       for(ProductTargetDTO targetDTO:targetList){
                           row1 = sheet.createRow(rowNum);
                           rowNum++;
                           cell = row1.createCell(0);
                           cell.setCellValue(sceneName);
                           cell.setCellStyle(contentCellStyle);
                           cell = row1.createCell(1);
                           cell.setCellStyle(contentCellStyle);
                           cell.setCellValue(productSeries);
                           cell = row1.createCell(2);
                           cell.setCellStyle(contentCellStyle);
                           cell.setCellValue(targetDTO.getTargetName());
                           cell = row1.createCell(3);
                           cell.setCellStyle(contentCellStyle);
                           cell.setCellValue(targetDTO.getQtyYear()+"");
                           cell = row1.createCell(4);
                           cell.setCellStyle(contentCellStyle);
                           cell.setCellValue(targetDTO.getQtyMonth()+"");
                       }
                   }
               }
               //子链群的资源类型
               List<ContractXwType3DTO> childXwType3List = childTargetDTO.getChildXwType3List();
               if(null != childXwType3List && childXwType3List.size() > 0){
                   row1 = sheet.createRow(rowNum);
                   rowNum++;
                   cell = row1.createCell(0);
                   cell.setCellStyle(headerCellStyle);
                   cell.setCellValue("资源类型名称");
                   cell = row1.createCell(1);
                   cell.setCellValue("所需最小作战单元(个)");
                   cell.setCellStyle(headerCellStyle);
                   for(ContractXwType3DTO xwType3DTO:childXwType3List){
                       row1 = sheet.createRow(rowNum);
                       rowNum++;
                       cell = row1.createCell(0);
                       cell.setCellStyle(contentCellStyle);
                       cell.setCellValue(xwType3DTO.getXwType3());
                       cell = row1.createCell(1);
                       cell.setCellValue(xwType3DTO.getInputNumber());
                       cell.setCellStyle(contentCellStyle);
                   }
               }
           }
        }
        ByteArrayOutputStream bot = new ByteArrayOutputStream();
        workbook.write(bot);
        ExcelUtil.export(request,response,workbook,"举单详情.xls");
    }

    @Override
    public void exportChainProduct(String chainCode,String month,HttpServletRequest request, HttpServletResponse response) throws IOException {
        List list = new ArrayList<>();
        list = productChainDao.selectList(new QueryWrapper<ZProductChain>().eq("chain_code",chainCode).eq("month",month));
        Workbook workbook = new HSSFWorkbook();
        ExcelUtil.buildSheet(workbook, "链群爆款",list, TEMPLATE_TITLE_CHAIN_PRODUCT);
        ByteArrayOutputStream bot = new ByteArrayOutputStream();
        workbook.write(bot);
        ExcelUtil.export(request,response,workbook,"链群爆款.xls");
    }

    @Override
    public List<ChainProductDTO> getChainProductListByExcel(InputStream inputStream, String originalFilename) throws Exception {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        List list = new ArrayList<>();
        //创建Excel工作薄
        Workbook work = this.getWorkbook(inputStream, originalFilename);
        if (null == work) {
            throw new RException("Excle工作簿为空",Constant.CODE_VALIDFAIL);
        }
        sheet = work.getSheetAt(0);
        if (sheet == null) {
            throw new RException("Excle工作簿为空",Constant.CODE_VALIDFAIL);
        }
        for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
            row = sheet.getRow(j);
            if (row == null ) {
                continue;
            }
            if(row.getFirstCellNum() == j){
                String title1 = row.getCell(0)==null?"":row.getCell(0).getStringCellValue();
                String title2 = row.getCell(1)==null?"":row.getCell(1).getStringCellValue();
                String title3 = row.getCell(2)==null?"":row.getCell(2).getStringCellValue();
                String title4 = row.getCell(3)==null?"":row.getCell(3).getStringCellValue();
                String title5 = row.getCell(4)==null?"":row.getCell(4).getStringCellValue();
                String title6 = row.getCell(5)==null?"":row.getCell(5).getStringCellValue();
                if("链群编码".equals(title1)&&"月份（例202005）".equals(title2)&&"场景名称".equals(title3)&&"系列名称".equals(title4)&&"型号名称".equals(title5)&&"型号编码".equals(title6)){
                    continue;
                }else {
                    throw new RException("请先下载模板，再上传",Constant.CODE_VALIDFAIL);
                }
            }
            ChainProductDTO dto = new ChainProductDTO();
            for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                cell = row.getCell(y);
                if (cell != null) {
                    try{
                        CellType cellType = cell.getCellTypeEnum();
                        if (y == 0) dto.setChainCode(cell.getStringCellValue());
                        if (y == 1) {
                            if(CellType.NUMERIC.equals(cellType)){
                                dto.setMonth((cell.getNumericCellValue()+"").substring(0,6));
                            }else if(CellType.STRING.equals(cellType)){
                                dto.setMonth(cell.getStringCellValue());
                            }
                        }
                        if (y == 2) dto.setSceneName(cell.getStringCellValue());
                        if (y == 3) dto.setProductSeries(cell.getStringCellValue());
                        if (y == 4) dto.setModelName(cell.getStringCellValue());
                        if (y == 5) dto.setModelCode(cell.getStringCellValue());
                    }catch (IllegalStateException e){
                        throw new RException("第"+(j+1)+"行第"+(y+1)+"列,格式不正确",Constant.CODE_VALIDFAIL);
                    }
                }
            }
            list.add(dto);
        }

        work.close();
        return list;
    }

    @Override
    public void saveChainProduct(List<ChainProductDTO> list) {
        String chainCode="";
        String month="";
        if(null != list && list.size()>0){
            chainCode = list.get(0).getChainCode();
            month = list.get(0).getMonth();
            //校验链群编码是否存在
            List<ZHrChainInfo> chainInfos = hrChainInfoDao.selectList(new QueryWrapper<ZHrChainInfo>().eq("chain_code", chainCode).eq("deleted", "0"));
            if(null == chainInfos || chainInfos.size()==0) throw new RException("链群编码不存在",Constant.CODE_VALIDFAIL);
            productChainDao.delete(new QueryWrapper<ZProductChain>().eq("chain_code",chainCode).eq("month",month));
            for(ChainProductDTO dto:list){
                ZProductChain productChain = new ZProductChain();
                if(!chainCode.equals(dto.getChainCode())) throw new RException("链群编码不一致",Constant.CODE_VALIDFAIL);
                productChain.setChainCode(dto.getChainCode());
                productChain.setSceneName(dto.getSceneName());
                productChain.setMonth(dto.getMonth());
                productChain.setProductSeries(dto.getProductSeries());
                productChain.setModelCode(dto.getModelCode());
                productChain.setModelName(dto.getModelName());
                productChainDao.insert(productChain);
            }
        }

    }

    @Override
    public void updateContractDate(ContractDateUpdateDTO dto) throws Exception {

        //更新举单
        List<ZContracts> updateList=new ArrayList<>();
        List<ZContracts> grabList = new ArrayList<>();
        ZContracts contracts = contractsDao.selectByContractId(dto.getId());
        Date joinTime = contracts.getJoinTime();
        Date checkTime = contracts.getCheckTime();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(dto.getJoinTime() != null) contracts.setJoinTime(sf.parse(dto.getJoinTime()));
        if(dto.getCheckTime() != null) contracts.setCheckTime(sf.parse(dto.getCheckTime()));
        if(dto.getJoinTime() != null && sf.parse(dto.getJoinTime()).after(joinTime)){
            contracts.setStatus("0");
        }
        if(dto.getCheckTime() != null && sf.parse(dto.getCheckTime()).after(checkTime)
                && checkTime.before(new Date())){
            contracts.setIsChecked("0");
        }
        updateList.add(contracts);
        //查询子合约
        List<ZContracts> childList=contractsService.list(new QueryWrapper<ZContracts>().eq("contract_type","10").eq("parent_id",dto.getId()));
        if(null != childList && childList.size()>0 ){
            String parentId = "";
            for (ZContracts child:childList){
                parentId += child.getId() +";";
                if(dto.getJoinTime() != null) child.setJoinTime(sf.parse(dto.getJoinTime()));
                if(dto.getCheckTime() != null) child.setCheckTime(sf.parse(dto.getCheckTime()));
                if(dto.getJoinTime() != null && sf.parse(dto.getJoinTime()).after(joinTime)){
                    child.setStatus("0");
                }
                updateList.add(child);
            }
            //查询抢单
            grabList = contractsService.list(new QueryWrapper<ZContracts>()
                    .in("contract_type",new String[]{"20","30"})
                    .in("parent_id",parentId.split(";"))
            );
        }else {
            grabList=contractsService.list(new QueryWrapper<ZContracts>()
                    .in("contract_type",new String[]{"20","30"})
                    .eq("parent_id",dto.getId())
            );
        }

        //查询抢单
        for (ZContracts grab:grabList) {
            ZContracts updateContract=new ZContracts();
            updateContract.setId(grab.getId());
            if(dto.getJoinTime() != null) updateContract.setJoinTime(sf.parse(dto.getJoinTime()));
            if(dto.getCheckTime() != null) updateContract.setCheckTime(sf.parse(dto.getCheckTime()));
            if(dto.getCheckTime() != null && sf.parse(dto.getCheckTime()).after(checkTime)
                    && checkTime.before(new Date())&& "8".equals(grab.getStatus())){
                updateContract.setStatus("1");
                updateContract.setIsChecked("0");
            }
            updateList.add(updateContract);
        }
        if(updateList!=null&&updateList.size()>0) {
            contractsService.updateBatchById(updateList);
        }
    }

    @Override
    public List<ZProductChain> selectChainProductList() throws Exception{
        //获取当前用户
        Subject subject = SecurityUtils.getSubject();
        SysEmployeeEhr sysUser = (SysEmployeeEhr) subject.getPrincipal();
        String userCode = sysUser.getEmpSn();
        List<ZProductChain> list = productChainDao.selectChainProductList(userCode);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMM");
        if(null != list && list.size()>0){
            for(ZProductChain product : list){
                String periodCode = product.getMonth();
                Date date = sf.parse(periodCode);
                String firstDay = DateFormatUtil.getFirstDayOfMonth(date);
                String lastDay = DateFormatUtil.getLastDayOfMonth(date);
                product.setFirstDay(firstDay);
                product.setLastDay(lastDay);
            }
        }
        return list;
    }

    @Override
    public List<ZProductChain> selectChainProductDetail(ChainProductDTO chainProductDTO) {
        List<ZProductChain> list = productChainDao.selectList(new QueryWrapper<ZProductChain>()
                .eq("chain_code",chainProductDTO.getChainCode())
                .eq("month",chainProductDTO.getMonth()));
        return list;
    }

    //校验excle格式
    public Workbook getWorkbook(InputStream inStr, String fileName) throws Exception {
        Workbook workbook = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (".xls".equals(fileType)) {
            workbook = new HSSFWorkbook(inStr);
        } else if (".xlsx".equals(fileType)) {
            workbook = new XSSFWorkbook(inStr);
        } else {
            throw new RException("请上传excle文件",Constant.CODE_VALIDFAIL);
        }
        return workbook;
    }


    private static final ExcelUtil.CellHeadField[] TEMPLATE_TITLE_PRODUCT = {
            new ExcelUtil.CellHeadField("系列名称", "productSeries"),
            new ExcelUtil.CellHeadField("年度销量(台)", ""),
            new ExcelUtil.CellHeadField("月度销量(台)", "")
    };

    private static final ExcelUtil.CellHeadField[] TEMPLATE_TITLE_CHAIN_PRODUCT = {
            new ExcelUtil.CellHeadField("链群编码", "chainCode"),
            new ExcelUtil.CellHeadField("月份（例202005）", "month"),
            new ExcelUtil.CellHeadField("场景名称", "sceneName"),
            new ExcelUtil.CellHeadField("系列名称", "productSeries"),
            new ExcelUtil.CellHeadField("型号名称", "modelName"),
            new ExcelUtil.CellHeadField("型号编码", "modelCode")
    };
}
