package com.study.gulimall.search.service;

import com.study.gulimall.search.vo.SearchParam;
import com.study.gulimall.search.vo.SearchResult;

public interface MallSearchService  {

    SearchResult search(SearchParam searchParam);
}
