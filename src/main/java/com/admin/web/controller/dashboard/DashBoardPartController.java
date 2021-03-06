package com.admin.web.controller.dashboard;

import com.admin.web.controller.BaseController;
import com.admin.web.dto.Bucket;
import com.admin.web.service.DashBoardService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.filter.Filters;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilters;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class DashBoardPartController extends BaseController{

	@Autowired
	protected DashBoardService dashBoardService;

	private static final String VIEW_PATH = "dashboard/";
	private static final String COMPONENT_PATH = "component/";

	@RequestMapping( value = "/dashboard/statistics", method = RequestMethod.GET )
	public ModelAndView statsDashboard(Model model){
		return new ModelAndView(VIEW_PATH+"dashboard_statistics");
	}

	@RequestMapping( value = "/component/gauge/count", method = RequestMethod.GET )
	@ResponseBody
	public Bucket gaugeCount(Model model
			, @RequestParam(value="appDivision", defaultValue = "", required = false) String appDivision
			, @RequestParam(value="startDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
			, @RequestParam(value="endDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate	){
		SearchResponse searchResponse = dashBoardService.getGaugeTotalCount(appDivision, startDate, endDate);
		if(null != searchResponse){
			Bucket bucket = new Bucket();
			bucket.setKey("totalCount");
			bucket.setDocCount(searchResponse.getHits().totalHits);
			return  bucket;
		}
		return null;
	}

	@RequestMapping( value = "/component/grape/IosAndroid", method = RequestMethod.GET )
	@ResponseBody
	public   Map<String, List<Bucket>> GrapeIosAndroid(Model model
			, @RequestParam(value="appDivision", defaultValue = "", required = false) String appDivision
			, @RequestParam(value="startDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
			, @RequestParam(value="endDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate	){
		SearchResponse searchResponse = dashBoardService.getGrapeIosAndroid(appDivision, startDate, endDate);
		if(null != searchResponse && null != searchResponse.getAggregations()){
			Histogram aggregations = searchResponse.getAggregations().get("agg");
			Map<String, List<Bucket>> resultMap = new HashMap<String, List<Bucket>>();
			for (Histogram .Bucket entry : aggregations.getBuckets()) {
				ParsedFilters filter = entry.getAggregations().get("agg");
				List<Bucket> buckets = new ArrayList<Bucket>();
				buckets.add(new Bucket("Android", filter.getBucketByKey("Android").getDocCount()));
				buckets.add(new Bucket("iOS", filter.getBucketByKey("iOS").getDocCount()));
				resultMap.put(entry.getKeyAsString(), buckets);
			}
			return  resultMap;
		}
		return null;
	}

	@RequestMapping( value = "/component/piechart/os", method = RequestMethod.GET )
	@ResponseBody
	public  List<Bucket> pieChartOs(Model model
			, @RequestParam(value="appDivision", defaultValue = "", required = false) String appDivision
			, @RequestParam(value="startDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
			, @RequestParam(value="endDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate	){
		SearchResponse searchResponse = dashBoardService.getPieChartOs(appDivision, startDate, endDate);
		if(null != searchResponse && null != searchResponse.getAggregations()){
			Filters agg = searchResponse.getAggregations().get("agg");
			List<Bucket> results = new ArrayList<Bucket>();
			for (Filters.Bucket entry : agg.getBuckets()) {
				Bucket bucket = new Bucket();
				bucket.setKey(entry.getKeyAsString());
				bucket.setDocCount(entry.getDocCount());
				results.add(bucket);
			}
			return  results;
		}
		return null;
	}

	@RequestMapping( value = "/component/piechart/version", method = RequestMethod.GET )
	@ResponseBody
	public  List<Bucket> pieChartVersion(Model model
			, @RequestParam(value="appDivision", defaultValue = "", required = false) String appDivision
			, @RequestParam(value="startDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
			, @RequestParam(value="endDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate	){
		SearchResponse searchResponse = dashBoardService.getPieChartVersion(appDivision, startDate, endDate);
		if(null != searchResponse && null != searchResponse.getAggregations()){
			Terms agg = searchResponse.getAggregations().get("agg");
			List<Bucket> results = new ArrayList<Bucket>();
			for (Terms .Bucket entry : agg.getBuckets()) {
				Bucket bucket = new Bucket();
				bucket.setKey(entry.getKeyAsString());
				bucket.setDocCount(entry.getDocCount());
				results.add(bucket);
			}
			return  results;
		}
		return null;
	}

	@RequestMapping( value = "/component/piechart/device", method = RequestMethod.GET )
	@ResponseBody
	public  List<Bucket> pieChartDevice(Model model
			, @RequestParam(value="appDivision", defaultValue = "", required = false) String appDivision
			, @RequestParam(value="startDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate
			, @RequestParam(value="endDate", defaultValue = "", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate	){
		SearchResponse searchResponse = dashBoardService.getPieChartDevice(appDivision, startDate, endDate);
		if(null != searchResponse && null != searchResponse.getAggregations()){
			Terms agg = searchResponse.getAggregations().get("agg");
			List<Bucket> results = new ArrayList<Bucket>();
			for (Terms .Bucket entry : agg.getBuckets()) {
				Bucket bucket = new Bucket();
				bucket.setKey(entry.getKeyAsString());
				bucket.setDocCount(entry.getDocCount());
				results.add(bucket);
			}
			return  results;
		}
		return null;
	}
}
