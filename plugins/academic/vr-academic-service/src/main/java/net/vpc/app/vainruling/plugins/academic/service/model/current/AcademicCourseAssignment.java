/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.service.model.current;

import net.vpc.app.vainruling.api.model.AppDepartment;
import net.vpc.app.vainruling.api.ui.UIConstants;
import net.vpc.app.vainruling.plugins.academic.service.model.config.AcademicTeacher;
import net.vpc.upa.FormulaType;
import net.vpc.upa.UserFieldModifier;
import net.vpc.upa.config.Entity;
import net.vpc.upa.config.Field;
import net.vpc.upa.config.Formula;
import net.vpc.upa.config.Id;
import net.vpc.upa.config.Path;
import net.vpc.upa.config.Properties;
import net.vpc.upa.config.Property;
import net.vpc.upa.config.Sequence;

/**
 *
 * @author vpc
 */
@Entity(listOrder = "name")
@Path("Education/Load")
public class AcademicCourseAssignment {

    @Id
    @Sequence

    private int id;

    @Formula(
            value = "concat((select a.name from AcademicCoursePlan a where a.id=this.coursePlanId),'-',(select a.name from AcademicCourseType a where a.id=this.courseTypeId))",
            type = {FormulaType.PERSIST, FormulaType.UPDATE}
    )
    private String name;

    @Field(modifiers = {UserFieldModifier.MAIN})
    @Formula(
            value = "concat((select a.fullName from AcademicCoursePlan a where a.id=this.coursePlanId),'-',(select a.name from AcademicCourseType a where a.id=this.courseTypeId))",
            type = {FormulaType.PERSIST, FormulaType.UPDATE}
    )
    private String fullName;

    private String name2;
    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:10%")
    )
    private AcademicTeacher teacher;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private double valueC;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private double valueTD;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private double valueTP;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private double valuePM;
//    private double valueDU;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private double valueEffWeek;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private double groupCount = 1;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:40px")
    )
    private AcademicCourseType courseType;
    /**
     * number of teachers or Masters sharing the same Course load
     */
    private double shareCount = 1;

    private AcademicCoursePlan coursePlan;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Properties(
            @Property(name = UIConstants.Grid.COLUMN_STYLE, value = "width:10%")
    )
    private AppDepartment ownerDepartment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AcademicTeacher getTeacher() {
        return teacher;
    }

    public void setTeacher(AcademicTeacher teacher) {
        this.teacher = teacher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public double getValueC() {
        return valueC;
    }

    public void setValueC(double valueC) {
        this.valueC = valueC;
    }

    public double getValueTD() {
        return valueTD;
    }

    public void setValueTD(double valueTD) {
        this.valueTD = valueTD;
    }

    public double getValueTP() {
        return valueTP;
    }

    public void setValueTP(double valueTP) {
        this.valueTP = valueTP;
    }

    public double getValuePM() {
        return valuePM;
    }

    public void setValuePM(double valuePM) {
        this.valuePM = valuePM;
    }

    public double getValueEffWeek() {
        return valueEffWeek;
    }

    public void setValueEffWeek(double valueEffWeek) {
        this.valueEffWeek = valueEffWeek;
    }

    public double getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(double groupCount) {
        this.groupCount = groupCount;
    }

    public AcademicCourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(AcademicCourseType courseType) {
        this.courseType = courseType;
    }

    public double getShareCount() {
        return shareCount;
    }

    public void setShareCount(double shareCount) {
        this.shareCount = shareCount;
    }

    public AcademicCoursePlan getCoursePlan() {
        return coursePlan;
    }

    public void setCoursePlan(AcademicCoursePlan coursePlan) {
        this.coursePlan = coursePlan;
    }

    @Override
    public String toString() {
        return "AcademicCourseAssignment{" + "id=" + id + ", name=" + name + ", name2=" + name2 + ", teacher=" + teacher + ", valueC=" + valueC + ", valueTD=" + valueTD + ", valueTP=" + valueTP + ", valuePM=" + valuePM + ", valueEffWeek=" + valueEffWeek + ", groupCount=" + groupCount + ", courseType=" + courseType + ", shareCount=" + shareCount + ", coursePlan=" + coursePlan + '}';
    }

    public AppDepartment getOwnerDepartment() {
        return ownerDepartment;
    }

    public void setOwnerDepartment(AppDepartment ownerDepartment) {
        this.ownerDepartment = ownerDepartment;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}