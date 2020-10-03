package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private GoodsRepository goodsRepository;

    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        //查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        //查询分类
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //查询spu下的所有sku集合
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String,Object> map = new HashMap<>();
            map.put("ownSpec",sku.getOwnSpec());
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.isBlank(sku.getImages())? "":StringUtils.split(sku.getImages(),",")[0]);
            skuMapList.add(map);
        });

        //查询所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        // 查询spuDetail。获取规格参数值
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        //获取通用的规格参数
        Map<Long, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {});

        // 获取特殊的规格参数
        Map<Long,List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(),new TypeReference<Map<Long,List<Object>>>(){});

        // 定义map接收{规格参数名，规格参数值}
        Map<String, Object> paramMap = new HashMap<>();
        params.forEach(param ->{
            if(param.getGeneric()){
                String value = genericSpecMap.get(param.getId()).toString();
                if(param.getNumeric()){
                    // 如果是数值的话，判断该数值落在那个区间
                    value = chooseSegment(value, param);
                }
                paramMap.put(param.getName(),value);
            }else {
                paramMap.put(param.getName(),specialSpecMap.get(param.getId()));
            }
        });

        //设置参数
        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());

        goods.setAll(spu.getTitle()+" "+brand.getName()+" "+ StringUtils.join(names," "));
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        goods.setSpecs(paramMap);

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {

        String key = request.getKey();
        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if(StringUtils.isBlank(key)){
            return null;
        }
        //构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //对key进行全文检索查询
        //QueryBuilder baseQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        BoolQueryBuilder baseQuery = buildBooleanQueryBuilder(request);
        queryBuilder.withQuery(baseQuery);
        
        //通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));

        //排序
        String sortBy = request.getSortBy();
        Boolean desc = request.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc? SortOrder.DESC:SortOrder.ASC));
        }

        //分页查询
        Integer page = request.getPage();
        Integer size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1,size));

        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //执行搜索，获取结果集
        AggregatedPage<Goods> search = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        List<Map<String,Object>> categories = getCategoryAggResult(search.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAggResult(search.getAggregation("brands"));

        // 判断分类聚合的结果集大小，等于1则聚合
        List<Map<String,Object>> specs = null;
        if(!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            specs = getParamAggResult((Long)categories.get(0).get("id"),baseQuery);
        }

        return new SearchResult(search.getTotalElements(),search.getTotalPages(),search.getContent(),categories,brands,specs);
    }

    /**
     * 构建bool查询构建器
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBooleanQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));

        //添加过滤条件
        if(CollectionUtils.isEmpty(request.getFilter())){
            return  boolQueryBuilder;
        }
        for (Map.Entry<String, String> entry : request.getFilter().entrySet()) {
            String key = entry.getKey();
            if(StringUtils.equals("品牌",key)){
                // 如果过滤条件是“品牌”, 过滤的字段名：brandId
                key = "brandId";
            }else if(StringUtils.equals("分类",key)){
                // 如果是“分类”，过滤字段名：cid3
                key = "cid3";
            }else {
                // 如果是规格参数名，过滤字段名：specs.key.keyword
                key = "specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    /**
     * 聚合出规格参数过滤条件
     * @param id
     * @param baseQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long id, QueryBuilder baseQuery) {
        //构建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //基于基本的查询条件，聚合规格参数
        queryBuilder.withQuery(baseQuery);
        List<SpecParam> params = this.specificationClient.queryParams(null, id, null, true);
        //添加聚合
        params.forEach(param ->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName())
                    .field("specs."+param.getName()+".keyword"));
        });
        // 只需要聚合结果集，不需要查询结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        // 执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        // 定义一个集合，收集聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();

        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()){
            Map<String,Object> map = new HashMap<>();
            //放入规格参数名
            map.put("k",entry.getKey());
            // 收集规格参数值
            List<Object> options = new ArrayList<>();
            // 解析每个聚合
            StringTerms terms = (StringTerms)entry.getValue();
            // 遍历每个聚合中桶，把桶中key放入收集规格参数的集合中
            terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            map.put("options",options);
            paramMapList.add(map);
        }
        return paramMapList;
    }


    /**
     * 解析品牌聚合结果集
     * @param brands
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation brands) {
        LongTerms terms = (LongTerms)brands;
        return terms.getBuckets().stream().map(bucket -> this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue())).collect(Collectors.toList());
    }

    /**
     * 解析分类聚合结果集
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms = (LongTerms)aggregation;

        Map<String,Object> map = new HashMap<>();
        return terms.getBuckets().stream().map(bucket -> {
            long cid3 = bucket.getKeyAsNumber().longValue();
            List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(cid3));
            map.put("id",cid3);
            map.put("name",names.get(0));
            return map;
        }).collect(Collectors.toList());
    }

    public void createIndex(Long id) throws IOException {
        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }

    public void deleteIndex(Long id) {
        this.goodsRepository.deleteById(id);
    }
}
