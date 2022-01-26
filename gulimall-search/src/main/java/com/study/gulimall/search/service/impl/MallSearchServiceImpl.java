package com.study.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.common.to.es.SkuEsModel;
import com.study.common.utils.R;
import com.study.gulimall.search.config.GulimallElasticSearchConfig;
import com.study.gulimall.search.constant.EsConstant;
import com.study.gulimall.search.feign.ProductFeignService;
import com.study.gulimall.search.service.MallSearchService;
import com.study.gulimall.search.vo.AttrResponseVo;
import com.study.gulimall.search.vo.BrandVo;
import com.study.gulimall.search.vo.SearchParam;
import com.study.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {

        SearchResult result=null;
        //准备检索请求
        SearchRequest searchRequest=buildSearchRequrest(searchParam);
        try {
            //执行检索请求
            SearchResponse response= client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            result=buildSearchResult(response,searchParam);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response,SearchParam searchParam) {

        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels=new ArrayList<>();
        if(hits.getHits()!=null && hits.getHits().length>0){
            for (SearchHit hit:hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel=JSON.parseObject(sourceAsString,SkuEsModel.class);
                if(!StringUtils.isEmpty(searchParam.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);



        List<SearchResult.AttrVo> attrVos=new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName= ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        List<SearchResult.BrandVo> brandVos=new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = bucket.getKeyAsNumber().longValue();
            String brand_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            String brand_img_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brand_name_agg);
            brandVo.setBrandImg(brand_img_agg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos=new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket:buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        result.setPageNum(searchParam.getPageNum());
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int totalPages = (int)total%EsConstant.PRODUCT_PAGESIZE==0?(int)total%EsConstant.PRODUCT_PAGESIZE:((int)total/EsConstant.PRODUCT_PAGESIZE+1);
        result.setTotalPages(totalPages);

        //System.out.println(total);
        //System.out.println(totalPages);

        List<Integer> pageNavs=new ArrayList<>();
        for(int i=1;i<=totalPages;i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        if(searchParam.getAttrs()!=null && searchParam.getAttrs().size()>0){
            List<SearchResult.NavVo> collect = searchParam.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if(r.getCode()==0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                }else {
                    navVo.setNavName(s[0]);
                }
                String replace = replaceQueryString(searchParam, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());



            result.setNavs(collect);
        }

        if(searchParam.getBrandId()!=null && searchParam.getBrandId().size()>0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandsInfo(searchParam.getBrandId());

            if(r.getCode()==0){
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer=new StringBuffer();
                String replace="";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getName()+";");
                    replace = replaceQueryString(searchParam, brandVo.getBrandId()+"","brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
            }





            navs.add(navVo);
        }


        return result;
    }

    private String replaceQueryString(SearchParam searchParam, String value,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode =encode.replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return searchParam.get_queryString().replace("&"+key+"=" + encode, "");
    }


    private SearchRequest buildSearchRequrest(SearchParam searchParam) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        if(searchParam.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        if(searchParam.getBrandId()!=null && searchParam.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }

        if(searchParam.getAttrs()!=null && searchParam.getAttrs().size()>0){

            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId =s[0];
                String[] attrValues = s[1].split(":");
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        if(searchParam.getHasStock()!=null){
            boolQuery.filter(QueryBuilders.termsQuery("hasStock",searchParam.getHasStock()==1));
        }
        if(!StringUtils.isEmpty(searchParam.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if(s.length==2){
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if (s.length==1){
                if(searchParam.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if(searchParam.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        sourceBuilder.query(boolQuery);

        if(!StringUtils.isEmpty(searchParam.getSort())){
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            SortOrder order=s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0],order);
        }

        sourceBuilder.from((searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName")).size(1);
        sourceBuilder.aggregation(catalog_agg);

        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println(s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }
}
