package com.assignment.runner;

import com.assignment.model.RegistrationResponse;
import com.assignment.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private WebhookService webhookService;

    private final String NAME = "Pranjal Gosavi";
    private final String REG_NO = "ADT23SOCB0760";
    private final String EMAIL = "pranjalgosavi2507@gmail.com";

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Starting Bajaj Finserv Health Qualifier Flow <<<");

        try {
            System.out.println("Registering user: " + NAME + " (" + REG_NO + ")");
            RegistrationResponse response = webhookService.generateWebhook(NAME, REG_NO, EMAIL);
            
            if (response == null || response.getWebhook() == null) {
                System.err.println("Failed to receive valid registration response.");
                return;
            }

            String webhookUrl = response.getWebhook();
            String accessToken = response.getAccessToken();
            System.out.println("Registration Successful!");
            System.out.println("Webhook URL: " + webhookUrl);

            String sqlQuery = resolveSqlQuery(REG_NO);
            System.out.println("Resolved SQL Query based on regNo (" + REG_NO + "):");
            System.out.println(sqlQuery);

            System.out.println("Submitting solution to webhook...");
            webhookService.submitSolution(webhookUrl, accessToken, sqlQuery);
            System.out.println(">>> Submission Completed Successfully! <<<");

        } catch (Exception e) {
            System.err.println("An error occurred during the flow: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String resolveSqlQuery(String regNo) {
        String digitsOnly = regNo.replaceAll("\\D+", "");
        if (digitsOnly.isEmpty()) return "/* No digits found in regNo */";
        
        int lastValue = Integer.parseInt(digitsOnly);
        
        if (lastValue % 2 != 0) {
            return "SELECT p.AMOUNT AS SALARY, " +
                   "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                   "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                   "d.DEPARTMENT_NAME " +
                   "FROM PAYMENTS p " +
                   "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                   "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                   "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                   "ORDER BY p.AMOUNT DESC " +
                   "LIMIT 1;";
        } else {
            return "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                   "(SELECT COUNT(*) FROM EMPLOYEE e2 WHERE e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB) AS YOUNGER_EMPLOYEES_COUNT " +
                   "FROM EMPLOYEE e1 " +
                   "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                   "ORDER BY e1.EMP_ID DESC;";
        }
    }
}
