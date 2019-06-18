package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;
import entity.Result;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /*显示全部结果*/
    public List<TbBrand> findAll();

    /*显示分页结果*/
    public PageResult findPage(int pageNum,int pageSize);

    /*增加品牌*/
    public void add(TbBrand brand);

    /*修改品牌*/
    public void update(TbBrand brand);

    /*根据id获取实体*/
    public TbBrand findOne(Long id);

    /*删除品牌*/
    public void delete(long[] ids);

    /*品牌条件查询*/
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    List<Map> selectOptionList();

}
