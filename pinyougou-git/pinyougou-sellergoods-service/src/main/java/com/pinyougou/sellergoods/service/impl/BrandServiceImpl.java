package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService{
    @Autowired
    private TbBrandMapper brandMapper;

    /*显示数据*/
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
        return new PageResult(page.getResult(),page.getTotal());
    }

    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void delete(long[] ids) {
        for(long id:ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    //条件查询
    public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        TbBrandExample example=new TbBrandExample();
        TbBrandExample.Criteria criteria =example.createCriteria();
        if(brand!=null){
            //品牌不为空的时候分情况判断
            //品牌名不为空且有值的时候
            if(brand.getName()!=null && brand.getName().length()>0){
                //根据通配符匹配
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            //品牌名为空，首字母不为空的时候
            if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
        return new PageResult(page.getResult(),page.getTotal());
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }




}
