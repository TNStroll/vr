/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.service.model;

import net.vpc.upa.config.*;

/**
 * @author taha.bensalah@gmail.com
 */
@Entity(listOrder = "mainPeriod.name desc")
@Path("Admin/Config")
public class AppConfig {

    @Id
    private int id = 1;
    @Main
    private AppPeriod mainPeriod;
    @Summary
    private AppCompany mainCompany;

    public AppConfig() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AppPeriod getMainPeriod() {
        return mainPeriod;
    }

    public void setMainPeriod(AppPeriod mainPeriod) {
        this.mainPeriod = mainPeriod;
    }

    public AppCompany getMainCompany() {
        return mainCompany;
    }

    public void setMainCompany(AppCompany mainCompany) {
        this.mainCompany = mainCompany;
    }

}
