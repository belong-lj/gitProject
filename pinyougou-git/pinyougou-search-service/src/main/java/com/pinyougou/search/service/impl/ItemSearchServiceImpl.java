package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map=new HashMap<>();
/*        Query query=new SimpleQuery();
        //添加查询条件
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", page.getContent());//getContent返回当前结果集*/
        //空格处理
        String keywords =(String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));
        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3.查询品牌和规格列表
        String category= (String) searchMap.get("category");
        if(!category.equals("")){//如果有分类名称
            map.putAll(searchBrandAndSpecList(category));
        }else{//如果没有分类名称，按照第一个查询
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //创建私有方法，用于返回查询列表（高亮）的结果
    //根据关键字搜索列表
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
        //构建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//设置高亮前缀
        highlightOptions.setSimplePostfix("</em>");//设置高亮后缀
        //为查询对象设置高亮选项
        query.setHighlightOptions(highlightOptions);//设置高亮选项


        //1.1按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按分类筛选
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap=(Map)searchMap.get("spec");
            for(String key:specMap.keySet()){
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(searchMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //1.5按价格筛选
        if(!"".equals(searchMap.get("price"))){//如果价格不等于空字符串
            String[] price =((String)searchMap.get("price")).split("-");
            //如果区间起点price[0]（最低价）不等于0,大于等于
            if(!price[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            //如果区间终点price[1]（最高价格）不等于*，小于
            if(!price[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThan(price[1]);
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //1.6分页查询
        //提取页码，如果没选择页码，默认第一页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;
        }
        //每页记录数，如果没选择，默认20条记录
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize==null){
            pageSize=20;
        }
        //从第几条记录开始查询
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);


        //1.7排序
        String sortValue = (String) searchMap.get("sort");//排序规则ASC  DESC
        String sortField = (String) searchMap.get("sortField");//需要进行排序的字段
        if(sortValue!=null && !sortValue.equals("")){
            if(sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
                query.addSort(sort);
            }
        }

        //***********  获取高亮结果集  ***********
        //高亮页对象，返回高亮的集合
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获得高亮入口集合
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for(HighlightEntry<TbItem> entry:entryList){
            //获取高亮列表(取决于高亮域的数量)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
            for(HighlightEntry.Highlight h:highlightList) {
/*                //获取内容
                List<String> snipplets = h.getSnipplets();//每个域有可能存储多个值
                System.out.println(snipplets );
            }*/

                if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                    TbItem item = entry.getEntity();//获取原实体类
                    item.setTitle(highlightList.get(0).getSnipplets().get(0));//设置高亮的结果
                }
            }
        }

        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        return map;

    }

    //商品分类列表，分组查询
    private List<String> searchCategoryList(Map searchMap){
        List<String> list =new ArrayList<>();
        Query query=new SimpleQuery("*:*");
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//相当于where
        query.addCriteria(criteria);

        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//相当于group by
        query.setGroupOptions(groupOptions);
        
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //通过列得到分组结果集
        GroupResult<TbItem> groupResult  = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /*查询品牌和规格列表*/
    private Map searchBrandAndSpecList(String category){
        HashMap map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        if(typeId!=null){
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //根据模板ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }
}

