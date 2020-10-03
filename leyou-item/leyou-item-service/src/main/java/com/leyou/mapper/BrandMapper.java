package com.leyou.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 新增分类和品牌中间表数据
     * @param bid
     * @param cid
     */
    @Insert("insert into tb_category_brand (category_id,brand_id) values (#{cid},#{bid})")
    void insertCategoryAndBrand(@Param("bid") Long bid, @Param("cid") Long cid);

    @Select("select * from tb_brand where id in (select brand_id from tb_category_brand where category_id = #{cid})")
    List<Brand> selectBrandsByCid(Long cid);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    void deleteCategoryAndBrand(@Param("bid")Long bid);
}
