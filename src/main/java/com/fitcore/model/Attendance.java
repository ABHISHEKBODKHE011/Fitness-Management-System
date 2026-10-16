package com.fitcore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing daily gym check-in records.
 */
public class Attendance {
    private Integer id;
    private Integer memberId;
    private LocalDateTime checkInTime;
    private LocalDate checkInDate;
    private String notes;
    private LocalDateTime createdAt;

    // Display helpers
    private String memberCode;
    private String memberName;
    private String memberPhone;
    private String membershipStatus;

    public Attendance() {
        this.checkInTime = LocalDateTime.now();
        this.checkInDate = LocalDate.now();
    }

    public Attendance(Integer id, Integer memberId, LocalDateTime checkInTime, LocalDate checkInDate, String notes) {
        this.id = id;
        this.memberId = memberId;
        this.checkInTime = checkInTime != null ? checkInTime : LocalDateTime.now();
        this.checkInDate = checkInDate != null ? checkInDate : LocalDate.now();
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }
}
