package com.leyou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.mapper.BrandMapper;
import com.leyou.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class BrandServiceImpl implements BrandService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @Override
    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        // 初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //根据name模糊查询，或者根据首字母查询
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+ key +"%").orEqualTo("letter",key);
        }

        //添加分页条件
        PageHelper.startPage(page,rows);

        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc?"desc":"asc"));
        }

        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装成pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);


        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //增加brand表
        this.brandMapper.insertSelective(brand);

        //增加中间表
        cids.forEach(cid-> this.brandMapper.insertCategoryAndBrand(brand.getId(),cid));
    }

    /**
     * 根据分类id查询品牌
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.selectBrandsByCid(cid);
    }

    /**
     * 根据brandId查询品牌
     * @param id
     * @return
     */
    @Override
    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void deleteBrandById(Long id) {
        //删除中间表数据
        this.brandMapper.deleteCategoryAndBrand(id);
        //删除品牌表数据
        this.brandMapper.deleteByPrimaryKey(id);
    }
}
