package com.leyou.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.service.impl.SpecificationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationServiceImpl specificationService;

    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid")Long cid){
        List<SpecGroup> groups = this.specificationService.queryGroupsByCid(cid);
        if(CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * 查询规格参数
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "generic",required = false)Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching
    ){
        List<SpecParam> params = this.specificationService.queryParams(gid,cid,generic,searching);
        if(CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }

    /**
     * 根据分类id查询规格参数集合
     * @param cid
     * @return
     */
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsWithParam(@PathVariable("cid")Long cid){
        List<SpecGroup> groups = this.specificationService.queryGroupsWithParam(cid);
        if(CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    @PostMapping(value = "/group")
    public ResponseEntity<Void> saveGroup(@RequestBody SpecGroup group){
        this.specificationService.saveGroup(group);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/group/{id}")
    public ResponseEntity<Void> deleteGroupById(@PathVariable("id") Long id){
        this.specificationService.deleteGroupById(id);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
