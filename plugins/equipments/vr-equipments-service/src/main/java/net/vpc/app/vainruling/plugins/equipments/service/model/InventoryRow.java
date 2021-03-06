/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.equipments.service.model;

import net.vpc.app.vainruling.core.service.model.AppArea;
import net.vpc.app.vainruling.core.service.model.AppUser;
import net.vpc.upa.FormulaType;
import net.vpc.upa.RelationshipType;
import net.vpc.upa.config.*;

import java.util.Date;

/**
 * @author taha.bensalah@gmail.com
 */
@Entity(listOrder = "date desc")
@Path("Equipment/Inventory")
public class InventoryRow {

    @Id
    @Sequence
    private int id;
    @ManyToOne(type = RelationshipType.COMPOSITION)
    @Summary
    private Inventory inventory;
    @Summary
    private AppUser user;
    @Summary
    @Formula(value = "CurrentTimestamp()", type = {FormulaType.PERSIST, FormulaType.UPDATE})
    private Date date;
    @Summary
    private Equipment equipment;
    @Summary
    private AppArea area;
    @Summary
    private double expectedQuantity;
    @Summary
    private Double quantity1;
    @Summary
    private Double quantity2;
    @Summary
    private Double quantity3;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public AppArea getArea() {
        return area;
    }

    public void setArea(AppArea area) {
        this.area = area;
    }

    public Double getQuantity1() {
        return quantity1;
    }

    public void setQuantity1(Double quantity1) {
        this.quantity1 = quantity1;
    }

    public Double getQuantity2() {
        return quantity2;
    }

    public void setQuantity2(Double quantity2) {
        this.quantity2 = quantity2;
    }

    public Double getQuantity3() {
        return quantity3;
    }

    public void setQuantity3(Double quantity3) {
        this.quantity3 = quantity3;
    }

    public double getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(double expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }


}
