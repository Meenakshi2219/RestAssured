package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
 
public class ExtentManager {
 
    private static ExtentReports extent;
 
    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            ExtentSparkReporter spark =
                    new ExtentSparkReporter("target/extent-report/ExtentReport.html");
            spark.config().setReportName("API Automation Report");
            spark.config().setDocumentTitle("Test Results");
 
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Tester", "Kavithaa");
        }
        return extent;
    }
}
