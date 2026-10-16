package com.fitcore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing a gym member.
 */
public class Member {
    private Integer id;
    private String memberCode;
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    private String emergencyContact;
    private LocalDate joiningDate;
    private Integer assignedTrainerId;
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Associated/Display fields
    private String assignedTrainerName;
    private String currentMembershipStatus; // ACTIVE, EXPIRED, NONE
    private String currentPlanName;
    private LocalDate currentMembershipEndDate;

    public Member() {
        this.status = "ACTIVE";
        this.joiningDate = LocalDate.now();
    }

    public Member(Integer id, String memberCode, String fullName, String phone, String email, String gender, String emergencyContact, LocalDate joiningDate, Integer assignedTrainerId, String status) {
        this.id = id;
        this.memberCode = memberCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.emergencyContact = emergencyContact;
        this.joiningDate = joiningDate != null ? joiningDate : LocalDate.now();
        this.assignedTrainerId = assignedTrainerId;
        this.status = status != null ? status : "ACTIVE";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Integer getAssignedTrainerId() {
        return assignedTrainerId;
    }

    public void setAssignedTrainerId(Integer assignedTrainerId) {
        this.assignedTrainerId = assignedTrainerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAssignedTrainerName() {
        return assignedTrainerName;
    }

    public void setAssignedTrainerName(String assignedTrainerName) {
        this.assignedTrainerName = assignedTrainerName;
    }

    public String getCurrentMembershipStatus() {
        return currentMembershipStatus;
    }

    public void setCurrentMembershipStatus(String currentMembershipStatus) {
        this.currentMembershipStatus = currentMembershipStatus;
    }

    public String getCurrentPlanName() {
        return currentPlanName;
    }

    public void setCurrentPlanName(String currentPlanName) {
        this.currentPlanName = currentPlanName;
    }

    public LocalDate getCurrentMembershipEndDate() {
        return currentMembershipEndDate;
    }

    public void setCurrentMembershipEndDate(LocalDate currentMembershipEndDate) {
        this.currentMembershipEndDate = currentMembershipEndDate;
    }

    @Override
    public String toString() {
        return memberCode + " - " + fullName;
    }
}
