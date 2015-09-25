/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.inventory.service;

import net.vpc.app.vainruling.api.AppPlugin;
import net.vpc.app.vainruling.api.CorePlugin;
import net.vpc.app.vainruling.api.Install;
import net.vpc.app.vainruling.plugins.inventory.service.model.Inventory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author vpc
 */
@AppPlugin(
        version = "1.0", dependsOn = {"equipmentPlugin"}
)
public class InventoryPlugin {

    @Autowired
    CorePlugin core;

    @Install
    public void installService() {
//        AppProfile technicianProfile;
//        technicianProfile = new AppProfile();
//        technicianProfile.setName("Technician");
//        technicianProfile = core.insertOrUpdate(technicianProfile);
//
//        AppProfile headOfDepartment;
//        headOfDepartment = new AppProfile();
//        headOfDepartment.setName(CorePlugin.HEAD_OF_DEPARTMENT);
//        headOfDepartment = core.insertOrUpdate(headOfDepartment);
//
//        for (net.vpc.upa.Entity ee : UPA.getPersistenceUnit().getPackage("Equipment/Inventory").getEntities(true)) {
//            core.profileAddRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Persist");
//            core.profileAddRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Remove");
//            core.profileAddRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Update");
//            core.profileAddRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Navigate");
//            core.profileAddRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Load");
//
//            core.profileAddRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Persist");
//            core.profileAddRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Update");
//            core.profileAddRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Navigate");
//            core.profileAddRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Load");
//        }
    }

    @Install
    public void installSemoService() {
//        Inventory v = new Inventory();
//        v.setName("INV-2015");
//        v = core.insertOrUpdate(v);
    }
}