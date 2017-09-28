package com.tr.legalintegration.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tr.legalintegration.model.Judge;
import com.tr.legalintegration.template.Templates;
import com.tr.legalintegration.utils.CSVUtils;

@SpringBootApplication
@EnableEurekaClient
@RestController
public class DataManagmentApplication {
	
	private static final String DEMANDING = "DEMANDING";
	private static final String DEFENDANT = "DEFENDANT";

	
	public static void main(String[] args) {
		SpringApplication.run(DataManagmentApplication.class, args);
	}
	
	
	@RequestMapping(value = "/judge", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
	public String judge( 
	        @RequestParam(value="name", required=true) String nameJudge,
	        @RequestParam(value="country", required=true) String country,
	        @RequestParam(value="subject", required=false) String subject	   
			) {
	
		//Search judge in CSV
		String csvFile = "//bue-itsfscm-a01/CMSTemp/cases-analytics/DB_cases_analytics.csv";
		List<Judge> listJudge = search(csvFile,nameJudge,country,subject);
				
		//Modify template
		String content=buildTemplate(listJudge);
		
		return content;
	}
	
	
	
	private List<Judge> search(String csvFile, String nameJudge,String country,String subject) {
		List<Judge> result= new ArrayList<Judge>();
		
		//Read CSV
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(csvFile),"iso-8859-1");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (scanner.hasNext()) {
	          List<String> line = CSVUtils.parseLine(scanner.nextLine());
	    
	          if (line.get(0).isEmpty()){
	        	  break;
	          }
	          
	          if (line.get(1).equalsIgnoreCase(nameJudge) && line.get(2).equalsIgnoreCase(country)){
	        		 
	        	  if (subject == null){
	        		  result.add(createJudge(line));
	        		  
	        	  }else{
	        		  if (subject!=null && line.get(3).equalsIgnoreCase(subject)){
	        			  result.add(createJudge(line));
		        	  }
	        	  }
	        }
	    }
	    scanner.close();
		
		return result;
	}

	private Judge createJudge(List<String> line) {
		Judge judge = new Judge();
		
		judge.setId(line.get(0));
		judge.setName(line.get(1));
		judge.setCountry(line.get(2));
		judge.setSubject(line.get(3));
		judge.setCaratula(line.get(4));
		judge.setCourt(line.get(5));
		judge.setDemanding(line.get(6));
		judge.setDefendant(line.get(7));
		judge.setSource(line.get(8));
		judge.setIdVeredict(line.get(9));
		judge.setDateVeredict(line.get(10));
		judge.setVeredict(line.get(11));
		
		return judge;
	}
	
	
	

	private String buildTemplate(List<Judge> listJudge) {
		
		//Get template
		Templates templates = new Templates();
		String template = templates.callTemplate("pieAndColumn");
		
		//Modify Title
		if (listJudge.isEmpty()){
			return template.replace("$TITLE", "Search not found");
		}
		template = template.replace("$TITLE", listJudge.get(0).getName());	
		
		//Data for pie chart
		StringBuilder dataPieChart = buildFirstRowPieChart();
		int demanding = 0;
		int defendant = 0;
		
		//Data for year column chart
		StringBuilder dataByYear = buildFirstRowColumnChart("Year");
		TreeMap<String,HashMap<String,Integer>> mapaDataYearChart = new TreeMap<String,HashMap<String,Integer>>() ;
		
		//Data for caratula column chart
		StringBuilder dataByCaratula = buildFirstRowColumnChart("Caratula");
		TreeMap<String,HashMap<String,Integer>> mapaDataCaratulaChart = new TreeMap<String,HashMap<String,Integer>>() ;
		
		for(Judge judge:listJudge){
			String year = judge.getDateVeredict().split("/")[2];
			String caratula = judge.getCaratula();
			
			if (judge.getVeredict().equals(DEMANDING)){
				demanding++;
			}else{
				defendant++;
			}
						
			mapaDataYearChart = updateMapa(mapaDataYearChart,year,judge.getVeredict());
			mapaDataCaratulaChart = updateMapa(mapaDataCaratulaChart,caratula,judge.getVeredict());
		
		}
		
		//Set data pie chart in template
		dataPieChart.append(buildDataForPieChart(demanding, defendant));
		template = template.replace("$DATAPIE", dataPieChart.toString());
		
		//Set data year column chart in template
		dataByYear.append(buildDataForColumnChart(mapaDataYearChart));
		template = template.replace("$DATABYYEAR", dataByYear.toString());
		
		//Set data year column chart in template
		dataByCaratula.append(buildDataForColumnChart(mapaDataCaratulaChart));
		template = template.replace("$DATABYCARATULA", dataByCaratula.toString());
		
		return template;
	}


	private TreeMap<String, HashMap<String, Integer>> updateMapa(
			TreeMap<String, HashMap<String, Integer>> mapaData,String key, String veredict ) {
		
		if (mapaData.get(key)==null){
			HashMap<String,Integer> value = new HashMap<String,Integer>();
			value.put(DEMANDING, 0);
			value.put(DEFENDANT, 0);
			mapaData.put(key, value);
		}
		
		HashMap<String,Integer> value = mapaData.get(key);
		
		if (veredict.equals(DEMANDING)){
			value.put(DEMANDING, value.get(DEMANDING)+1);
		}else{
			value.put(DEFENDANT, value.get(DEFENDANT)+1);
		}
		
		mapaData.put(key,value);
		
		return mapaData;
	}


	private StringBuilder buildFirstRowColumnChart(String nameColumn) {
		StringBuilder dataColumnChart= new StringBuilder();
		dataColumnChart.append("[");
		dataColumnChart.append("'"+nameColumn+"'");
		dataColumnChart.append(",");
		dataColumnChart.append("'"+DEMANDING+"'");
		dataColumnChart.append(",");
		dataColumnChart.append("'"+DEFENDANT+"'");
		dataColumnChart.append("],");
		return dataColumnChart;
	}

	private StringBuilder buildFirstRowPieChart() {
		StringBuilder dataPieChart= new StringBuilder();
		dataPieChart.append("[");
		dataPieChart.append("'Sentence'");
		dataPieChart.append(",");
		dataPieChart.append("'Quantity'");
		dataPieChart.append("],");
		return dataPieChart;
	}

	private StringBuilder buildDataForColumnChart(TreeMap<String,HashMap<String,Integer>> mapaDataColumnChart) {
		StringBuilder data= new StringBuilder();
		for (Entry<String, HashMap<String, Integer>> entry : mapaDataColumnChart.entrySet()) {
		    String key = entry.getKey();
		    HashMap<String, Integer> value = entry.getValue();
		    
		    data.append("[");
			data.append("'"+key+"'");
			data.append(",");
			data.append(value.get(DEMANDING));
			data.append(",");
			data.append(value.get(DEFENDANT));
			data.append("],");
		}
		
		return data;
	}

	private StringBuilder buildDataForPieChart(int demanding,int defendant) {
		StringBuilder data= new StringBuilder();
		
		data.append("[");
	    data.append("'"+DEMANDING+"'");
	    data.append(",");
	    data.append(demanding);
	    data.append("],");
	    
	    data.append("[");
	    data.append("'"+DEFENDANT+"'");
	    data.append(",");
	    data.append(defendant);
	    data.append("],");
		
		return data;
	}

}
