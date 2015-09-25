/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.service.model.current;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author vpc
 */
public class AcademicCourseAssignmentInfo {

    private AcademicCourseAssignment assignment;
    private boolean assigned;
    private String intents;
    private Set<String> intentsSet = Collections.EMPTY_SET;
    private Set<Integer> intentsUserIdsSet = Collections.EMPTY_SET;

    public AcademicCourseAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(AcademicCourseAssignment assignment) {
        this.assignment = assignment;
    }

    public String getIntents() {
        return intents;
    }

    public void setIntents(String intents) {
        this.intents = intents;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public Set<String> getIntentsSet() {
        return intentsSet;
    }

    public void setIntentsSet(Set<String> intentsSet) {
        this.intentsSet = intentsSet;
    }

    public Set<Integer> getIntentsUserIdsSet() {
        return intentsUserIdsSet;
    }

    public void setIntentsUserIdsSet(Set<Integer> intentsUserIdsSet) {
        this.intentsUserIdsSet = intentsUserIdsSet;
    }

}