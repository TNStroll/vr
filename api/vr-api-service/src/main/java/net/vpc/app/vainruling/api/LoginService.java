/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.api;

import net.vpc.app.vainruling.api.security.UserSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.vpc.app.vainruling.api.model.AppUser;
import net.vpc.app.vainruling.api.model.AppProfile;
import net.vpc.upa.Action;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.UPA;
import net.vpc.upa.types.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vpc
 */
@Service
public class LoginService {

    @Autowired
    private TraceService trace;

    public UserSession getUserSession() {
        return VrApp.getContext().getBean(UserSession.class);
    }

    public void logout() {
        final UserSession s = getUserSession();
        if (s.isImpersonating()) {
            trace.trace("logout", "successfull logout " + s.getUser().getLogin() + " to " + s.getRootUser().getLogin(),
                    s.getUser().getLogin() + " => "
                    + s.getRootUser().getLogin(),
                    getClass().getSimpleName(), null, null, s.getUser().getLogin(), s.getUser().getId(), Level.INFO, s.getClientIpAddress()
            );
            s.setUser(s.getRootUser());
            s.setRootUser(null);
            buildSession(s, s.getUser());
        } else {
            trace.trace("logout", "successfull logout " + s.getUser().getLogin(),
                    s.getUser().getLogin(),
                    getClass().getSimpleName(), null, null, s.getUser().getLogin(), s.getUser().getId(), Level.INFO, s.getClientIpAddress()
            );
            VrApp.getBean(ActiveSessionsTracker.class).onDestroy(s);
        }
    }

    public AppUser impersonate(String login, String password) {
        UserSession s = getUserSession();
        if (s.isAdmin() && !s.isImpersonating()) {
            AppUser user = findEnabledUser(login, password);
            if (user != null) {
                trace.trace("impersonate", "successfull impersonate of " + login, login, getClass().getSimpleName(), null, null, s.getUser().getLogin(),
                        s.getUser().getId(), Level.INFO, s.getClientIpAddress()
                );
            } else {
                user = findUser(login);
                if (user != null) {
                    if (!user.isEnabled()) {
                        trace.trace("impersonate", "successfull impersonate of " + login + ". but user is not enabled!", login, getClass().getSimpleName(), null, null, s.getUser().getLogin(), s.getUser().getId(), Level.WARNING, s.getClientIpAddress()
                        );
                    } else {
                        trace.trace("impersonate", "successfull impersonate of " + login + ". but password " + password + " seems not to be incorrect", login, getClass().getSimpleName(), null, null, s.getUser().getLogin(), s.getUser().getId(), Level.WARNING, s.getClientIpAddress());
                    }
                } else {
                    trace.trace(
                            "impersonate", "failed impersonate of " + login, login, getClass().getSimpleName(), null, null, s.getUser().getLogin(), s.getUser().getId(), Level.SEVERE, s.getClientIpAddress()
                    );
                }
            }
            if (user != null) {
                s.setRootUser(s.getUser());
                s.setUser(user);
                buildSession(s, user);
            }
            return user;
        } else {
            trace.trace(
                    "impersonate", "failed impersonate of " + login + ". not admin or already impersonating", login, getClass().getSimpleName(), null, null, s.getUser().getLogin(), s.getUser().getId(), Level.WARNING, s.getClientIpAddress()
            );
        }
        return null;
    }

    public AppUser login(String login, String password) {
        final AppUser user = findEnabledUser(login, password);
        if (user != null) {
            user.setConnexionCount(user.getConnexionCount() + 1);
            user.setLastConnexionDate(new DateTime());
            UPA.getContext().invokePrivileged(
                    TraceService.makeSilenced(
                            new Action<Object>() {

                        @Override
                        public Object run() {
                            UPA.getPersistenceUnit().merge(user);
                            return null;
                        }
                    }), null);
            UserSession s = getUserSession();
            s.setDestroyed(false);
            VrApp.getBean(ActiveSessionsTracker.class).onCreate(s);
            trace.trace("login", "successfull", login, getClass().getSimpleName(), null, null, login, user.getId(), Level.INFO, s.getClientIpAddress());
            getUserSession().setConnexionTime(user.getLastConnexionDate());
            getUserSession().setUser(user);
            buildSession(s, user);
        } else {
            UserSession s = getUserSession();
            s.reset();
            AppUser user2 = findUser(login);
            if (user2 == null) {
                trace.trace("login", "login not found. Failed as " + login + "/" + password, login + "/" + password, getClass().getSimpleName(), null, null, (login == null || login.length() == 0) ? "anonymous" : login, -1, Level.SEVERE, s.getClientIpAddress());
            } else if (user2.isDeleted() || !user2.isEnabled()) {
                trace.trace("login", "invalid state. Failed as " + login + "/" + password, login + "/" + password
                        + ". deleted=" + user2.isDeleted()
                        + ". enabled=" + user2.isEnabled(), getClass().getSimpleName(), null, null, (login == null || login.length() == 0) ? "anonymous" : login, user2.getId(), Level.SEVERE, s.getClientIpAddress()
                );
            } else {
                trace.trace(
                        "login", "invalid password. Failed as " + login + "/" + password, login + "/" + password,
                        getClass().getSimpleName(), null, null, (login == null || login.length() == 0) ? "anonymous" : login, user2.getId(),
                        Level.SEVERE, s.getClientIpAddress()
                );
            }
        }
        return user;
    }

    protected void buildSession(UserSession s, AppUser user) {
        CorePlugin core = VrApp.getBean(CorePlugin.class);
        final List<AppProfile> userProfiles = findUserProfiles(user.getId());
        Set<String> userProfilesNames = new HashSet<>();
        for (AppProfile u : userProfiles) {
            userProfilesNames.add(u.getName());
        }
        getUserSession().setProfiles(userProfiles);
        StringBuilder ps = new StringBuilder();
        for (AppProfile up : userProfiles) {
            if (ps.length() > 0) {
                ps.append(", ");
            }
            ps.append(up.getName());
        }
        getUserSession().setProfileNames(userProfilesNames);
        getUserSession().setProfilesString(ps.toString());
        getUserSession().setAdmin(false);
        getUserSession().setRights(core.findUserRights(user.getId()));
        if (user.getLogin().equalsIgnoreCase("admin") || userProfilesNames.contains(CorePlugin.PROFILE_ADMIN)) {
            getUserSession().setAdmin(true);
        }

    }

    private AppUser findUser(String login) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu.findByField(AppUser.class, "login", login);
    }

    private List<AppProfile> findUserProfiles(int userId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId")
                .setParameter("userId", userId)
                .getEntityList();
    }

    private AppUser findUser(String login, String password) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu
                .createQuery("Select u from AppUser u "
                        + "where "
                        + "u.login=:login "
                        + "and u.password=:password")
                .setParameter("login", login)
                .setParameter("password", password)
                .getEntity();
    }

    private AppUser findEnabledUser(String login, String password) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu
                .createQuery("Select u from AppUser u "
                        + "where "
                        + "u.login=:login "
                        + "and u.password=:password "
                        + "and u.enabled=true "
                        + "and u.deleted=false "
                        + "")
                .setParameter("login", login)
                .setParameter("password", password)
                .getEntity();
    }

}
