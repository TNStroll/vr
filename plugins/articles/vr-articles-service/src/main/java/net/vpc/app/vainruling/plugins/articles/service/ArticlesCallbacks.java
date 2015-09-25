/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.articles.service;

import net.vpc.app.vainruling.api.util.VrHelper;
import net.vpc.app.vainruling.plugins.articles.service.model.ArticlesItem;
import net.vpc.upa.Entity;
import net.vpc.upa.Field;
import net.vpc.upa.callbacks.DefinitionListenerAdapter;
import net.vpc.upa.callbacks.EntityEvent;
import net.vpc.upa.callbacks.FieldDefinitionListener;
import net.vpc.upa.callbacks.FieldEvent;
import net.vpc.upa.config.Callback;
import net.vpc.upa.exceptions.UPAException;

/**
 *
 * @author vpc
 */
@Callback
public class ArticlesCallbacks extends DefinitionListenerAdapter 
    implements FieldDefinitionListener{

    @Override
    public void onCreateField(FieldEvent event) throws UPAException {
        Entity e = event.getEntity();
        Field f = event.getField();
        if(e.getEntityType().equals(ArticlesItem.class) && f.getName().equals("sender")){
            f.setDefaultObject(VrHelper.DEFAULT_OBJECT_CURRENT_USER);
        }
    }

    
    @Override
    public void onCreateEntity(EntityEvent event) {
    }
    
}