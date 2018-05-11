package com.admin.web.service.impl;

import com.admin.web.controller.LoginController;
import com.admin.web.dto.HitSource;
import com.admin.web.service.DashBoardService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashBoardServiceImpl implements DashBoardService {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${elasticsearch.host}") private String host;
    @Value("${elasticsearch.port}") private String port;
    @Value("${elasticsearch.search.size}") private String searchSize;

    @Override
    public SearchResponse getGrapeRealtime(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
            );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            ExtendedBounds extendedBounds = new ExtendedBounds( String.valueOf(sDate), String.valueOf(eDate) );
            AggregationBuilder aggregation =
                    AggregationBuilders
                            .dateHistogram("agg")
                            .field("@timestamp")
                            .minDocCount(0)
                            .extendedBounds(extendedBounds)
                            .dateHistogramInterval(DateHistogramInterval.MINUTE);
            searchSourceBuilder.aggregation(aggregation);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            client.close();
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<HitSource> getTableRealtime(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
            );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            searchSourceBuilder.size(Integer.parseInt(searchSize));
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest);
            List<SearchHit[]> totalHits = new ArrayList<SearchHit[]>();
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            totalHits.add(searchHits);
            List<HitSource> results = new ArrayList<HitSource>();
            if(!CollectionUtils.isEmpty(totalHits)){
                for(SearchHit[] hits : totalHits){
                    for(SearchHit hit : hits){
                        HitSource source = new HitSource();
                        Map<String, Object> map = hit.getSourceAsMap();
                        source.setDevice_model( (String)map.get("device_model") );
                        source.setApp_gubun( (String)map.get("app_gubun") );
                        source.setOs_version( (String)map.get("os_version") );
                        source.setIp( (String)map.get("ip") );
                        source.setUuid( (String)map.get("uuid") );
                        source.setErr_message( (String)map.get("err_message") );
                        source.setErr_time( (String)map.get("err_time") );
                        source.setRefer( (String)map.get("refer") );
                        source.setErr_name( (String)map.get("err_name") );
                        source.setApp_ver( (String)map.get("app_ver") );
                        source.setDevice_gubun( (String)map.get("device_gubun") );
                        source.setNetwork_type( (String)map.get("network_type") );
                        source.setCustomer_id( (String)map.get("customer_id") );
                        results.add(source);
                    }
                }
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponse getGaugeTotalCount(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
            );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            AggregationBuilder aggregation =
                    AggregationBuilders
                            .filters("agg",
                                    new FiltersAggregator.KeyedFilter("Android", QueryBuilders.termQuery("device_gubun.keyword", "Android")),
                                    new FiltersAggregator.KeyedFilter("iOS", QueryBuilders.termQuery("device_gubun.keyword", "iOS")));
            searchSourceBuilder.aggregation(aggregation);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            client.close();
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponse getGrapeIosAndroid(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
                    );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            ExtendedBounds extendedBounds = new ExtendedBounds( String.valueOf(sDate), String.valueOf(eDate) );

            AggregationBuilder subAgg =
                    AggregationBuilders
                            .filters("agg",
                                    new FiltersAggregator.KeyedFilter("Android", QueryBuilders.termQuery("device_gubun.keyword", "Android")),
                                    new FiltersAggregator.KeyedFilter("iOS", QueryBuilders.termQuery("device_gubun.keyword", "iOS")));

            AggregationBuilder aggregation =
                    AggregationBuilders
                            .dateHistogram("agg")
                            .field("@timestamp")
                            .minDocCount(0)
                            .extendedBounds(extendedBounds)
                            .dateHistogramInterval(DateHistogramInterval.MINUTE)
                            .subAggregation(subAgg);
            searchSourceBuilder.aggregation(aggregation);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            client.close();
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponse getPieChartOs(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
                    );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            AggregationBuilder aggregation =
                    AggregationBuilders
                            .filters("agg",
                                    new FiltersAggregator.KeyedFilter("Android", QueryBuilders.termQuery("device_gubun.keyword", "Android")),
                                    new FiltersAggregator.KeyedFilter("iOS", QueryBuilders.termQuery("device_gubun.keyword", "iOS")));
            searchSourceBuilder.aggregation(aggregation);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            client.close();
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponse getPieChartVersion(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
            );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            AggregationBuilder aggregation =
                    AggregationBuilders
                            .terms("agg")
                            .field("app_ver.keyword");
            searchSourceBuilder.aggregation(aggregation);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            client.close();
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponse getPieChartDevice(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(host, Integer.parseInt(port)))
            );
            Map<String, LocalDateTime> DateMap = settingDate(startDate, endDate);
            LocalDateTime sDate = DateMap.get("startDate");
            LocalDateTime eDate= DateMap.get("endDate");

            SearchRequest searchRequest = new SearchRequest("appmon*");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(eDate).from(sDate));
            AggregationBuilder aggregation =
                    AggregationBuilders
                            .terms("agg")
                            .field("device_model.keyword");
            searchSourceBuilder.aggregation(aggregation);
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            client.close();
            return searchResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, LocalDateTime> settingDate(LocalDateTime startDate, LocalDateTime endDate){
        Map<String, LocalDateTime> map = new HashMap<String, LocalDateTime>();
        // 시간을 선택하지 않았을 경우
        if(StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate) ) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -9);
            LocalDateTime eDate = new Timestamp(cal.getTime().getTime()).toLocalDateTime();
            cal.add(Calendar.MINUTE, -10);
            LocalDateTime sDate = new Timestamp(cal.getTime().getTime()).toLocalDateTime();
            map.put("startDate", sDate);
            map.put("endDate", eDate);
        }else{
            map.put("startDate", startDate );
            map.put("endDate", endDate );
        }
        return map;
    }

}