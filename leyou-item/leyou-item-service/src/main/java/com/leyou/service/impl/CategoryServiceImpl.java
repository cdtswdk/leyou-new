package com.leyou.service.impl;

import com.leyou.item.pojo.Category;
import com.leyou.mapper.CategoryMapper;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CategoryServiceImpl implements CategoryService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父类id查询分类
     * @param pid
     * @return
     */
    @Override
    public List<Category> queryCategoriesByPid(Long pid){
        Category record = new Category();
        record.setParentId(pid);
        return categoryMapper.select(record);
    }

    /**
     * 根据品牌id查询分类
     * @param bid
     * @return
     */
    @Override
    public List<Category> queryCategoryByBid(Long bid) {
        return this.categoryMapper.queryCategoryByBid(bid);
    }

    /**
     * 根据id查询分类名称
     * @param ids
     * @return
     */
    @Override
    public List<String> queryNamesByIds(List<Long> ids) {
        List<Category> categories = this.categoryMapper.selectByIdList(ids);
        return categories.stream().map(Category::getName).collect(Collectors.toList());

    }

    @Override
    public List<Category> queryAllByCid3(Long id) {
        Category c3 = this.categoryMapper.selectByPrimaryKey(id);
        Category c2 = this.categoryMapper.selectByPrimaryKey(c3.getParentId());
        Category c1 = this.categoryMapper.selectByPrimaryKey(c2.getParentId());
        return Arrays.asList(c1,c2,c3);
    }
}
