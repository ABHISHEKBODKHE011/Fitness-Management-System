package com.fitcore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing trainer-member assignment history.
 */
public class TrainerAssignment {
    private Integer id;
    private Integer memberId;
    private Integer trainerId;
    private LocalDate assignedDate;
    private LocalDate unassignedDate;
    private String notes;
    private LocalDateTime createdAt;

    // Display helpers
    private String memberName;
    private String trainerName;

    public TrainerAssignment() {
        this.assignedDate = LocalDate.now();
    }

    public TrainerAssignment(Integer id, Integer memberId, Integer trainerId, LocalDate assignedDate, LocalDate unassignedDate, String notes) {
        this.id = id;
        this.memberId = memberId;
        this.trainerId = trainerId;
        this.assignedDate = assignedDate != null ? assignedDate : LocalDate.now();
        this.unassignedDate = unassignedDate;
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

    public Integer getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Integer trainerId) {
        this.trainerId = trainerId;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getUnassignedDate() {
        return unassignedDate;
    }

    public void setUnassignedDate(LocalDate unassignedDate) {
        this.unassignedDate = unassignedDate;
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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }
}
