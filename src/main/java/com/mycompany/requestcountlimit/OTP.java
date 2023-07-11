/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.requestcountlimit;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class OTP implements Serializable {
    @Id
    private String aadharNumber;
    private String otp;
    private LocalDateTime lastRequestTime;
    private int genrequestCount;
    private int valrequestCount;

    // Getters and setters

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public int getRequestCount() {
        return genrequestCount;
    }

    public void setRequestCount(int requestCount) {
        this.genrequestCount = requestCount;
    }

    public int getValrequestCount() {
        return valrequestCount;
    }

    public void setValrequestCount(int valrequestCount) {
        this.valrequestCount = valrequestCount;
    }
    
    
    public LocalDateTime getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(LocalDateTime lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}