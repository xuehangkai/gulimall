package com.study.gulimall.search.vo;

import com.study.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {

    private List<SkuEsModel> products;

    private Integer pageNum;

    private Long total;

    private Integer totalPages;

    private List<Integer> pageNavs;

    private List<BrandVo> brands;

    private List<AttrVo> attrs;

    private List<CatalogVo> catalogs;

    private List<NavVo> navs=new ArrayList<>();
    private List<Long> attrIds=new ArrayList<>();

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }


    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
