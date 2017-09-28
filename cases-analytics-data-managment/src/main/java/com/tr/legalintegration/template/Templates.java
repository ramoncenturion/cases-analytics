package com.tr.legalintegration.template;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.tr.legalintegration.model.Constant;

public class Templates {

	
	public String callTemplate(String nameTemplate){
		
		String contentTemplate ="";
		
		
		try {
			
			if (Constant.PIE_AND_COLUMN.equals(nameTemplate)){
				contentTemplate = Files.toString(new File("src/main/java/com/tr/legalintegration/template/templatePieAndColumnChart.html"), Charsets.UTF_8);
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return contentTemplate;
	}
}
