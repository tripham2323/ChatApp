/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class Model_Create_Group {
    private String groupName;
    private String groupDescription;
    private int[] memberIDs;
    private int createdBy;

    public Model_Create_Group() {
    }

    public Model_Create_Group(String groupName, String groupDescription, int[] memberIDs, int createdBy) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.memberIDs = memberIDs;
        this.createdBy = createdBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public int[] getMemberIDs() {
        return memberIDs;
    }

    public void setMemberIDs(int[] memberIDs) {
        this.memberIDs = memberIDs;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
