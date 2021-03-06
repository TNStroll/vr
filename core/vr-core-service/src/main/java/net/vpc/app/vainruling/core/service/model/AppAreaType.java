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
@Entity(listOrder = "name")
@Path("Admin/Config")
public class AppAreaType {

    @Id
    @Sequence
    private int id;
    @Main
    @Unique
    private String name;

    public AppAreaType() {
    }

    public AppAreaType(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return String.valueOf(name);
    }

}
