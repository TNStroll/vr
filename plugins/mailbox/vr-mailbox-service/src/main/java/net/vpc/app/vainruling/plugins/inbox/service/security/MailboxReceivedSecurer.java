/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.inbox.service.security;

import net.vpc.upa.DefaultEntitySecurityManager;
import net.vpc.upa.Entity;
import net.vpc.upa.config.SecurityContext;
import net.vpc.upa.exceptions.UPAException;
import net.vpc.upa.expressions.Expression;
import net.vpc.upa.expressions.UserExpression;

/**
 *
 * @author vpc
 */
@SecurityContext(entity = "MailboxReceived")
public class MailboxReceivedSecurer extends DefaultEntitySecurityManager{

    @Override
    public Expression getEntityFilter(Entity entity) throws UPAException {
        if(entity.getPersistenceUnit().getPersistenceGroup().getSecurityManager().getUserPrincipal().getName().equals("admin")){
            return null;
        }
        return new UserExpression("this.deleted=false and this.sender.login=currentUser()");
    }
    
}