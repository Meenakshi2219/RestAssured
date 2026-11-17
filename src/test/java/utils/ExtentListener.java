package utils;

import java.util.Arrays;

import org.testng.ITestListener;
import org.testng.ITestResult;
 
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
 
public class ExtentListener implements ITestListener {
 
    private static ExtentReports extent = ExtentManager.getExtent();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
 
    public static ExtentTest getTest() {
        return test.get();
    }
 
    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName()
                + " " + Arrays.toString(result.getParameters());
        test.set(extent.createTest(name));
    }
 
    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("Test Passed");
    }
 
    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(result.getThrowable());
    }
 
    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("Test Skipped");
    }
 
    @Override
    public void onFinish(org.testng.ITestContext context) {
        extent.flush();   
    }
}