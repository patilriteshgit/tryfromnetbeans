/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.requestcountlimit;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.json.JSONObject;

@WebServlet("/valreqcount")
public class valreqcount extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //String aadharNumber = request.getParameter("aadharNumber");
        //String otp = request.getParameter("otp");
         // Read the JSON data from the request body
        BufferedReader reader = request.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        reader.close();

        // Parse the JSON data to extract the Aadhar number and OTP
        JSONObject jsonObject = new JSONObject(jsonBody.toString());
        String aadharNumber = jsonObject.getString("aadharNumber");
        String otp = jsonObject.getString("otp");
        
        if (hasExceededRequestLimit(aadharNumber)) {
            sendErrorResponse(response, "Exceeded maximum request limit");
            return;
        }


        boolean isValid = otpIsValid(aadharNumber, otp);

        JSONObject jsonResponse = new JSONObject();

        if (isValid) {
            jsonResponse.put("message", "Authentication successful");

            // Deleting if success auth
            
            // added to delet record
            deleteOTP(aadharNumber);
        } else {
            jsonResponse.put("message", "Authentication failed");
        }

        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }

    private boolean otpIsValid(String aadharNumber, String otp) {
        String storedOTP = getOTPFromDatabase(aadharNumber);
        boolean isValid = otp.equals(storedOTP);
    
    if (!isValid) {
        incrementValRequestCount(aadharNumber);
    }
    
    return isValid;
    }

    private String getOTPFromDatabase(String aadharNumber) {
        Session session = null;
        String storedOTP = null;

        try {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            OTP otpEntity = session.get(OTP.class, aadharNumber);
            if (otpEntity != null) {
                storedOTP = otpEntity.getOtp();
            }

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return storedOTP;
    }
    //
    private boolean hasExceededRequestLimit(String aadharNumber) {
    Session session = sessionFactory.openSession();
    Transaction transaction = session.beginTransaction();

    OTP otpEntity = session.get(OTP.class, aadharNumber);
    if (otpEntity != null && otpEntity.getValrequestCount() >= 3) {
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

    
    private void incrementValRequestCount(String aadharNumber) {
    Session session = sessionFactory.openSession();
    Transaction transaction = session.beginTransaction();

    OTP otpEntity = session.get(OTP.class, aadharNumber);
    if (otpEntity != null) {
        int currentCount = otpEntity.getValrequestCount();
        otpEntity.setValrequestCount(currentCount + 1);
        session.update(otpEntity);
    }

    transaction.commit();
    session.close();
}

    
    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", errorMessage);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(errorResponse.toString());
        out.flush();
    }

    private void deleteOTP(String aadharNumber) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        OTP otpEntity = session.get(OTP.class, aadharNumber);
        if (otpEntity != null) {
            session.delete(otpEntity);
        }
        
        session.getTransaction().commit();
        session.close();
    }
}
