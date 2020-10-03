package com.leyou.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> queryGroupsByCid(Long cid);

    List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching);

    List<SpecGroup> queryGroupsWithParam(Long cid);

    void saveGroup(SpecGroup specGroup);

    void deleteGroupById(Long id);
}
