/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.inventory.service;

import java.util.List;
import net.vpc.app.vainruling.api.model.AppUser;
import net.vpc.app.vainruling.plugins.equipments.service.model.Equipment;
import net.vpc.app.vainruling.plugins.inventory.service.model.Inventory;
import net.vpc.app.vainruling.plugins.inventory.service.model.InventoryRow;
import net.vpc.app.vainruling.plugins.inventory.service.model.InventoryStatus;
import net.vpc.app.vainruling.plugins.inventory.service.model.InventoryUser;
import net.vpc.upa.Entity;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.callbacks.EntityListenerAdapter;
import net.vpc.upa.callbacks.EntityListener;
import net.vpc.upa.callbacks.PersistEvent;
import net.vpc.upa.config.Callback;

/**
 *
 * @author vpc
 */
@Callback
public class InventoryPluginCallback 
    extends EntityListenerAdapter 
    implements EntityListener {

    @Override
    public void onPersist(PersistEvent event) {
        PersistenceUnit pu = event.getPersistenceUnit();
        Entity entity = event.getEntity();
        if (entity.getName().equals("Inventory")) {
            List<AppUser> inventoryUsers = pu.createQuery("Select o.user from AppUserProfileBinding o where o.profile.name=:name").setParameter("name", "inventory").getEntityList();
            for (AppUser u : inventoryUsers) {
                InventoryUser r = new InventoryUser();
                r.setInventory((Inventory) event.getPersistedObject());
                r.setUser(u);
                pu.persist(r);
            }
        } else if (entity.getName().equals("InventoryUser")) {
            InventoryUser eu=(InventoryUser) event.getPersistedObject();
            List<Equipment> equipments = pu.createQuery("Select o from Equipment o").getEntityList();
            for (Equipment equipment : equipments) {
                 InventoryRow r = new InventoryRow();
                    r.setInventory(eu.getInventory());
                    r.setUser(eu.getUser());
                    r.setEquipment(equipment);
                    r.setExpectedQuantity(equipment.getQuantity());
                    r.setArea(equipment.getLocation());
                    pu.persist(r);
            }
            
        } else if (entity.getName().equals("Equipment")) {
            List<Inventory> currents = pu.createQuery("Select o from Inventory o where o.status <> :closed").setParameter("closed", InventoryStatus.CLOSED)
                    .getEntityList();
            Equipment eq=(Equipment) event.getPersistedObject();
            for (Inventory in : currents) {
                List<AppUser> inventoryUsers = pu.createQuery("Select o.user from InventoryUser o where o.inventory.id=:id").setParameter("id", in.getId()).getEntityList();
                for (AppUser u : inventoryUsers) {
                    InventoryRow r = new InventoryRow();
                    r.setInventory(in);
                    r.setUser(u);
                    r.setEquipment(eq);
                    r.setExpectedQuantity(eq.getQuantity());
                    r.setArea(eq.getLocation());
                    pu.persist(r);
                }
            }
        }
    }

}