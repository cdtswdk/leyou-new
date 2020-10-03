package com.leyou.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface BrandService {

    PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    void saveBrand(Brand brand, List<Long> cids);

    List<Brand> queryBrandsByCid(Long cid);

    Brand queryBrandById(Long id);

    void deleteBrandById(Long id);
}
