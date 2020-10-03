package com.leyou.service.impl;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.mapper.SpecGroupMapper;
import com.leyou.mapper.SpecParamMapper;
import com.leyou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpecGroupMapper groupMapper;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpecParamMapper paramMapper;

    @Override
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return groupMapper.select(record);
    }

    @Override
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.paramMapper.select(record);
    }

    @Override
    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        groups.forEach(group->{
            List<SpecParam> params = this.queryParams(group.getId(), null, null, null);
            group.setParams(params);
        });
        return groups;
    }

    @Override
    @Transactional
    public void saveGroup(SpecGroup specGroup) {
        this.groupMapper.insert(specGroup);
    }

    @Override
    @Transactional
    public void deleteGroupById(Long id) {
        this.groupMapper.deleteByPrimaryKey(id);
    }
}
