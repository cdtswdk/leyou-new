package com.leyou.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {

    List<Category> queryCategoriesByPid(Long pid);

    List<Category> queryCategoryByBid(Long bid);

    List<String> queryNamesByIds(List<Long> ids);

    List<Category> queryAllByCid3(Long id);
}
