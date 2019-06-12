package com.library.utils.testrail;

import com.library.utils.testrail.QAAnnotations.TestCaseInfo;
import com.library.utils.testrail.QAAnnotations.TestClassInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TestRailHandler {

	public static APIClient client;
	
	public TestRailHandler(String username, String password, String url) throws Exception {
		client = new APIClient(url);
		client.setUser(username);
		client.setPassword(password);
	}
	
	public APIClient getAPIClient(){
		return client;
	}
	
	public Long getProjectIDByProjectName(String projectName) throws Exception{
		
		JSONArray projects = (JSONArray) client.sendGet("get_projects");
		Long projectID = null;
		JSONObject project;
		
		//for each JSON object in JSON array, return the id if the project name is found
		for (int i=0; i < projects.size(); i++) {
			project = (JSONObject) projects.get(i);
			if (project.get("name").equals(projectName)){
				System.out.println("ID for project name '" + projectName + "' = " + project.get("id"));
				projectID = (Long)project.get("id");
				return projectID;
			}
		}
		//if project not found, print out valid project names in TestRail
		System.out.println("No id found for project '" + projectName + "'.  Project name must be one of the following:");
		
		for (int j=0; j < projects.size(); j++){
			project = (JSONObject) projects.get(j);
			System.out.println("\t" + project.get("name"));
		}
		return projectID;
	}
	
	public Long getSuiteIDBySuiteName(Long projectID, String suiteName) throws Exception {
		JSONArray suites = (JSONArray) client.sendGet("get_suites/" + projectID);
		
		Long id = null;
		JSONObject suite;
		
		//for each JSON object in JSON array, return the id if the project name is found
		for (int i=0; i < suites.size(); i++) {
			suite = (JSONObject) suites.get(i);
			if (suite.get("name").equals(suiteName)){
				System.out.println("ID for suite name '" + suiteName + "' = " + suite.get("id"));
				id = (Long)suite.get("id");
				return id;
			}
		}
		//if suite not found, print out valid suite names in TestRail
		System.out.println("No id found for suite '" + suiteName + "'.  Suite name must be one of the following:");
		
		for (int j=0; j < suites.size(); j++){
			suite = (JSONObject) suites.get(j);
			System.out.println("\t" + suite.get("name"));
		}
		return id;
	}
	
	public Long getSectionIDBySectionName(Long projectID, Long suiteID, String sectionName) throws Exception{
		
		//JSONObject sectionObject = (JSONObject) client.sendGet("get_sections/2&suite_id=5");
		JSONArray sections = (JSONArray) client.sendGet("get_sections/" + projectID + "&suite_id=" + suiteID);

		Long sectionID = null;
		JSONObject section;
		
		//for each JSON object in JSON array, return the id if the project name is found
		for (int i=0; i < sections.size(); i++) {
			section = (JSONObject) sections.get(i);
			if (section.get("name").equals(sectionName)){
				System.out.println("ID for section name '" + sectionName + "' = " + section.get("id"));
				sectionID = (Long)section.get("id");
				return sectionID;
			}
		}
		//if project not found, print out valid project names in TestRail
		System.out.println("No ID found for section '" + sectionName + "'.  Section name must be one of the following:");
		
		for (int j=0; j < sections.size(); j++){
			section = (JSONObject) sections.get(j);
			System.out.println("\t" + section.get("name"));
		}
		return sectionID;
	}
	
	
	public void addTestCase(int sectionID, String testCaseTitle, String testCaseDescription) throws Exception{
		Map<String, String> data = new HashMap<String, String>();
		data.put("title", testCaseTitle);
		data.put("custom_description", testCaseDescription);
		JSONObject r = (JSONObject) client.sendPost("add_case/" + sectionID, data);
		System.out.println(r.get("id"));
	}
	
	/* method to return TestClassInfo annotation in test class
	 * @param testClass name (including package) of test class to look for annotation in
	 * @return TestClassInfo annotation object with projectName, suiteName and sectionName elements if found
	 * 		and null if not found
	 */
	private TestClassInfo getTestClassAnnotation(String testClass) throws Exception{
		//check if test class annotation exists in the class
		if (TestRailHandler.class.getClassLoader().loadClass((testClass)).isAnnotationPresent(TestClassInfo.class)){
			TestClassInfo testClassAnno = TestRailHandler.class.getClassLoader().loadClass((testClass)).getAnnotation(TestClassInfo.class);
			return testClassAnno;
		} else {
			System.out.println("ERROR:  TestClassInfo annotation does not exist in test class '" + testClass + "'");
			return null;
		}
		
	}
		
	/* method to push test case metadata from test methods into TestRail.  
	 * @param testClass name (including package) of test class to load test case info from 
	 */
	public void pushTestInfoToTestRailByTestClass(String testClass) throws Exception {
		
		try{
			//get test class annotation
			TestClassInfo testClassAnno = getTestClassAnnotation(testClass);
			
			//if test class annotation exists, proceed
			if (testClassAnno != null) {
			
				//get sectionID using project, suite and section names from test class annotation
				Long sectionID = getSectionID(testClassAnno.projectName(),testClassAnno.suiteName(),testClassAnno.sectionName());
	
				//for each method in test class
				for (Method method : TestRailHandler.class.getClassLoader().loadClass((testClass)).getMethods()) {
					
					// check if test method annotation is present for the method
					if (method.isAnnotationPresent(TestCaseInfo.class)) {
						try {
							//TODO:  check if test case is new or existing
							//TODO:  if new
								// iterates through all the annotations available in the method
								Map<String, String> data = new HashMap<String, String>();
								for (TestCaseInfo anno : method.getDeclaredAnnotationsByType(TestCaseInfo.class)) {
									System.out.println("title =" + anno.title() + ", custom_description =" + anno.description()
											+ ", test method name =" + method.getName());
									//handle case where title length is > 255 char
									if (anno.title().length() > 255){
										System.out.println("ERROR:  Title for test method '" + method.getName() + "' exceeds TestRail length (255)");
									}
									else {
											
										data.put("title", anno.title());
										data.put("custom_description", anno.description());
										data.put("custom_automation_script_name", anno.title());
									
										JSONObject r = (JSONObject) client.sendPost("add_case/" + sectionID, data);
										System.out.println(r.get("id"));
										
										//TODO: write id back to test script??
									}
							//TODO:  if existing, update
							}
							
						} catch (Throwable ex) {
							ex.printStackTrace();
						}
					} else {
						System.out.println("WARNING:  TestCaseInfo annotation not found for test method '" + method.getName() + "'");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* method to return section ID to load test cases to
	 * @param projectName name of project in TestRail
	 * @param suiteName name of suite in TestRail
	 * @param sectionName name of sectino in TestRail
	 * @return TestRail section ID if found
	 * 		and null if not found
	 */
	public Long getSectionID(String projectName, String suiteName, String sectionName) throws Exception{
		
		Long sectionID = null;
		
		Long projectID = getProjectIDByProjectName(projectName);
		if (projectID != null) {
			//Get Suite ID
			Long suiteID = getSuiteIDBySuiteName(projectID, suiteName);
			if (suiteID != null) {
				//Get Section ID
				sectionID = getSectionIDBySectionName(projectID, suiteID, sectionName);
				if (sectionID != null) {
					//loadTestInfoBySectionID(sectionID, testScript);
					return sectionID;
				}		
			}
		}
		return sectionID;
	}
	
	public void getResultsForCase(String testRunID, String testCaseID) throws Exception{
		
		JSONArray testResults = (JSONArray) client.sendGet("get_results_for_case/" + testRunID + "/" + testCaseID);
		JSONObject testResult;
			
		for (int j=0; j < testResults.size(); j++){
			testResult = (JSONObject) testResults.get(j);
			System.out.println("Test Results for test run ID '" + testRunID 
					+ ", test case id ':" 
					+ "\n\tid: " + testResult.get("id") 
					+ " status_id: " + testResult.get("status_id"));
		}
	}
	
	public void getTestCaseIDs(String projectName, String suiteName, String sectionName) throws Exception{
		
		Long projectID = getProjectIDByProjectName(projectName);
		Long suiteID = getSuiteIDBySuiteName(projectID, suiteName);
		Long sectionID = getSectionIDBySectionName(projectID, suiteID, sectionName);
		
		JSONArray testCases = (JSONArray) client.sendGet("get_cases/" + projectID + "&suite_id=" + suiteID + "&section_id=" + sectionID);
		JSONObject testCase;
			
		for (int j=0; j < testCases.size(); j++){
			testCase = (JSONObject) testCases.get(j);
			System.out.println("\tid: " + testCase.get("id") + "  title: " + testCase.get("title"));
		}
	}
	
	private boolean doesTestCaseTitleExist(){
		boolean exists = false;
		return exists;
	}
	
	public Long getRunIdByTestRunName(String projectName, String testRunName) throws Exception {
		Long projectID = getProjectIDByProjectName(projectName);
		JSONArray r2 = (JSONArray) client.sendGet("get_runs/"+projectID);		
		Long id = null;
		JSONObject suite;
		
		//for each JSON object in JSON array, return the id if the project name is found
		for (int i=0; i < r2.size(); i++) {
			suite = (JSONObject) r2.get(i);
			if (suite.get("name").equals(testRunName)){
				System.out.println("ID for run name '" + testRunName + "' = " + suite.get("id"));
				id = (Long)suite.get("id");
				return id;
			}
		}
		//if suite not found, print out valid suite names in TestRail
		System.out.println("No id found for run Name '" + testRunName + "'.  Run Name name must be one of the following:");
		
		for (int j=0; j < r2.size(); j++){
			suite = (JSONObject) r2.get(j);
			System.out.println("\t" + suite.get("name")+" "+suite.get("id") );
		}
		return id;
	}
	
	public void updateResultToTestRail(int value, String case_Id, String runid) throws Exception {
	
		Map data = new HashMap();		
		data.put("status_id", new Integer(value));
		data.put("comment", "Test execution from Selenium");
		//createTestRun();
		try {
			System.out.println("add_result_for_case/" + runid + "/" + case_Id);
			JSONObject r1 = (JSONObject) client.sendPost("add_result_for_case/" + runid + "/" + case_Id, data);

		} catch (Exception e) {
			
			System.out.println("*** Case Id is not correct : "+case_Id+" Run ID "+runid);
			e.printStackTrace(); 
		}
	}
	
	public void addTestRunInTestRail(String projectName, String suiteName) throws Exception {

		Long projectID = getProjectIDByProjectName(projectName);
		Long suiteID = getSuiteIDBySuiteName(projectID, suiteName);
		Map data = new HashMap();		
		data.put("suite_id", new Long(suiteID));
		data.put("name", "DemoTestRun4");
		data.put("description", "description DemoTestRun3");
		data.put("assignedto_id", new Integer(1));
		data.put("include_all", true);
		//createTestRun();
		try {
			//System.out.println("add_result_for_case/" + runid + "/" + case_Id);
			JSONObject r1 = (JSONObject) client.sendPost("add_run/" + projectID, data);//

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
