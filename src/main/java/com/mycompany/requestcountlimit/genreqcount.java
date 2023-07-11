/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package com.mycompany.requestcountlimit;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Random;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.time.temporal.ChronoUnit;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.json.JSONObject;

@WebServlet("/genreqcount")
public class genreqcount extends HttpServlet {
    private SessionFactory sessionFactory;

    @Override
    public void init() {
        sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    }

    @Override
    public void destroy() {
        sessionFactory.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //String aadharNumber = request.getParameter("aadharNumber");
        // Read JSON data from the request body
        BufferedReader reader = request.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        reader.close();

        // Parse the JSON data to extract the Aadhar number
        JSONObject jsonObject = new JSONObject(jsonBody.toString());
        String aadharNumber = jsonObject.getString("aadharNumber");
        
        if (hasExceededRequestLimit(aadharNumber)) {
            sendErrorResponse(response, "Exceeded maximum request limit");
            return;
        }
      
        String otp = generateOTP();

        saveOrUpdateOTP(aadharNumber, otp);
        

        // Create JSON response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("aadharNumber", aadharNumber);
        jsonResponse.put("otp", otp);

        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void saveOrUpdateOTP(String aadharNumber, String otp) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        OTP otpEntity = session.get(OTP.class, aadharNumber);
        
        if (otpEntity == null) {
            otpEntity = new OTP();
            otpEntity.setAadharNumber(aadharNumber);
            otpEntity.setRequestCount(0);
        }
        // working anyways hence replace
        otpEntity.setOtp(otp);
        otpEntity.setLastRequestTime(LocalDateTime.now());
        otpEntity.setRequestCount(otpEntity.getRequestCount() + 1);

        session.saveOrUpdate(otpEntity);
        session.getTransaction().commit();
        session.close();
    }
    
    
   private boolean hasExceededRequestLimit(String aadharNumber) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        OTP otpEntity = session.get(OTP.class, aadharNumber);
        if (otpEntity != null && otpEntity.getRequestCount() >= 3) {
            LocalDateTime lastRequestTime = otpEntity.getLastRequestTime();
            long hoursElapsed = ChronoUnit.HOURS.between(lastRequestTime, LocalDateTime.now());

            if (hoursElapsed < 1) {
                transaction.commit();
                session.close();
                return true;
            }
        }

        transaction.commit();
        session.close();
        return false;
    }
   
   private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", errorMessage);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(errorResponse.toString());
        out.flush();
    }
   
}

