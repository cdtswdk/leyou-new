package com.leyou.controller;

import com.leyou.item.pojo.Category;
import com.leyou.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.List;

@Controller
@MapperScan("com.leyou.mapper")
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    /**
     * 根据父类id查询分类
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid",defaultValue = "0")Long pid){
        if(pid == null || pid<0){
            // 响应400，相当于ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
        if(CollectionUtils.isEmpty(categories)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据品牌id查询分类
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryCategoryByBid(@PathVariable("bid")Long bid){
        List<Category> categories = this.categoryService.queryCategoryByBid(bid);
        if(categories == null || categories.size()<1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return  ResponseEntity.ok(categories);
    }

    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids")List<Long> ids){
        List<String> names = this.categoryService.queryNamesByIds(ids);
        if(CollectionUtils.isEmpty(names)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);
    }

    /**
     * 根据3级分类id，查询1~3级的分类
     * @param id
     * @return
     */
    @GetMapping("all/level")
    public ResponseEntity<List<Category>> queryAllByCid3(@RequestParam("id") Long id){
        List<Category> list = this.categoryService.queryAllByCid3(id);
        if(list == null || list.size() < 1){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }
}
