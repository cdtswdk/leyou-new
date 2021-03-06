package com.leyou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import com.leyou.mapper.*;
import com.leyou.service.CategoryService;
import com.leyou.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpuMapper spuMapper;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SkuMapper skuMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsServiceImpl.class);
    /**
     * 分页查询商品信息
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult<SpuBo> queryGoodsByPage(String key, String saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //查询条件
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        if(StringUtils.isNotBlank(saleable)){
            criteria.andEqualTo("saleable",saleable);
        }

        //分页查询
        PageHelper.startPage(page,rows);
        //执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        //将spu转换成spubos
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            //设置属性
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
            spuBo.setCname(StringUtils.join(names, "->"));

            return spuBo;
        }).collect(Collectors.toList());

        //返回集合
        return new PageResult<>(pageInfo.getTotal(),spuBos);
    }

    /**
     * 新增商品
     * @param spuBo
     */
    @Transactional
    @Override
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);
        //新增spu_detail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);
        // 新增sku和库存
        saveSkuAndStock(spuBo);

        sendMessage(spuBo.getId(),"insert");
    }

    /**
     * 新增sku和商品库存
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        //新增sku
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku -> {
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            //新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    /**
     * 更新商品
     * @param spuBo
     */
    @Override
    public void updateGoods(SpuBo spuBo) {
        //查询以前的sku
        List<Sku> skuList = this.querySkusBySpuId(spuBo.getId());
        //如果以前存在，则删除
        if(!CollectionUtils.isEmpty(skuList)){
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            //删除以前的库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);

            //删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spuBo.getId());
            this.skuMapper.delete(record);
        }
        //新增sku和库存
        saveSkuAndStock(spuBo);

        //更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        // 更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMessage(spuBo.getId(),"update");
    }

    @Override
    public Sku querySkuBySkuId(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }

    private void sendMessage(Long id, String type) {
        try {
            this.amqpTemplate.convertAndSend("item."+type,id);
        }catch (Exception e){
            LOGGER.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }


    /**
     * 根据spuid查询sku集合
     * @param id
     * @return
     */
    @Override
    public List<Sku> querySkusBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        return this.skuMapper.select(sku);
    }

    /**
     * 根据spuId查询spuDetail详情
     * @param spuId
     * @return
     */
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }


    @Override
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

}
