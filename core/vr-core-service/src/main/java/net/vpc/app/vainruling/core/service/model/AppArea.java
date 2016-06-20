/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.service.model;

import net.vpc.app.vainruling.core.service.util.UIConstants;
import net.vpc.upa.UserFieldModifier;
import net.vpc.upa.config.*;

/**
 * @author vpc
 */
@Entity(listOrder = "name")
@Path("Admin/Config")
public class AppArea {

    @Id
    @Sequence
    private int id;
    @Field(modifiers = {UserFieldModifier.MAIN, UserFieldModifier.UNIQUE})
    private String name;
    @Field(modifiers = UserFieldModifier.SUMMARY, max = "4000")
    @Property(name = UIConstants.FIELD_FORM_CONTROL, value = UIConstants.ControlType.TEXTAREA)
    private String description;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    @Hierarchy
    private AppArea parent;

    @Field(modifiers = UserFieldModifier.SUMMARY)
    private AppAreaType type;

    public AppArea() {
    }

    public AppArea(String name, String description, AppAreaType type, AppArea parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.type = type;
    }

    public AppAreaType getType() {
        return type;
    }

    public void setType(AppAreaType type) {
        this.type = type;
    }

    public AppArea getParent() {
        return parent;
    }

    public void setParent(AppArea parent) {
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        if (parent != null) {
            return parent.toString() + "/" + String.valueOf(name);
        }
        return String.valueOf(name);
    }

}