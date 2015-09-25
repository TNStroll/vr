/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import net.vpc.app.vainruling.api.security.UserSession;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.vpc.app.vainruling.api.model.AppCivility;
import net.vpc.app.vainruling.api.model.AppCompany;
import net.vpc.app.vainruling.api.model.AppCountry;
import net.vpc.app.vainruling.api.model.AppDepartment;
import net.vpc.app.vainruling.api.model.AppGender;
import net.vpc.app.vainruling.api.model.AppIndustry;
import net.vpc.app.vainruling.api.model.AppUser;
import net.vpc.app.vainruling.api.model.AppProfile;
import net.vpc.app.vainruling.api.model.AppProfileRight;
import net.vpc.app.vainruling.api.model.AppProperty;
import net.vpc.app.vainruling.api.model.AppUserProfileBinding;
import net.vpc.app.vainruling.api.model.AppRightName;
import net.vpc.app.vainruling.api.model.AppUserType;
import net.vpc.app.vainruling.api.util.InSetEvaluator;
import net.vpc.upa.Entity;
import net.vpc.upa.EntityShield;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.QueryBuilder;
import net.vpc.upa.UPA;
import net.vpc.upa.expressions.Equals;
import net.vpc.upa.expressions.Literal;
import net.vpc.upa.expressions.Var;
import net.vpc.upa.impl.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author vpc
 */
@AppPlugin(version = "1.4")
public class CorePlugin {

    public static final String HEAD_OF_DEPARTMENT = "HeadOfDepartment";
    public static final Set<String> ADMIN_ENTITIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("Trace", "User", "UserProfile", "UserProfileBinding", "UserProfileRight")));
    @Autowired
    private TraceService trace;

//    private void init(){
//    }
    public UserSession getUserSession() {
        return VrApp.getContext().getBean(UserSession.class);
    }

    public AppUser findUser(String login) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu.findByField(AppUser.class, "login", login);
    }

    public AppUser findUser(int id) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu.findById(AppUser.class, id);
    }

    public AppUserType findUserType(String name) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUserType) pu.findByMainField(AppUserType.class, name);
    }

    public AppProfile findUserProfile(String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppProfile) pu.findByMainField(AppProfile.class, profileName);
    }

    public List<AppUser> findUserByProfileExpression(String profileExpression) {
        if (profileExpression == null) {
            profileExpression = "";
        }
        List<AppUser> u = new ArrayList<>();
        List<AppProfile> p = new ArrayList<>();

        List<AppUser> allUsers = findEnabledUsers();
        List<AppProfile> allProfiles = findProfiles();
        Map<String, Object> matchedProfilesAndUsers = new HashMap<String, Object>();
        for (AppUser x : allUsers) {
            matchedProfilesAndUsers.put(x.getLogin(), x);
        }
        for (AppProfile x : allProfiles) {
            matchedProfilesAndUsers.put(x.getName(), x);
        }

        for (String s : profileExpression.split(" ,;")) {
            if (s.length() > 0) {
                Object f = matchedProfilesAndUsers.get(s);
                if (f != null) {
                    if (f instanceof AppUser) {
                        u.add((AppUser) f);
                    } else if (f instanceof AppProfile) {
                        p.add((AppProfile) f);
                    }
                } else {
                    if (s.contains("*")) {
                        for (AppProfile pp : allProfiles) {
                            if (Strings.matchesSimpleExpression(pp.getName(), s)) {
                                p.add(pp);
                            }
                        }
                        for (AppUser uu : allUsers) {
                            if (Strings.matchesSimpleExpression(uu.getLogin(), s)) {
                                u.add(uu);
                            }
                        }
                    }
                }
            }
        }
        Map<Integer, AppUser> result = new HashMap<>();
        for (AppProfile pp : allProfiles) {
            for (AppUser uu : findUsersByProfile(pp.getId())) {
                result.put(uu.getId(), uu);
            }
        }
        for (AppUser uu : allUsers) {
            result.put(uu.getId(), uu);
        }
        return new ArrayList<>(result.values());
    }

    public boolean createRight(String rightName, String desc) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppRightName r = pu.findById(AppRightName.class, rightName);
        if (r != null) {
            return false;
        }
        r = new AppRightName();
        r.setName(rightName);
        r.setDescription(desc);
        pu.persist(r);
        return true;
    }

    public boolean addProfileRight(int profileId, String rightName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppProfile p = pu.findById(AppProfile.class, profileId);
        if (p == null) {
            throw new IllegalArgumentException("Profile not found " + profileId);
        }
        AppRightName r = pu.findById(AppRightName.class, rightName);
        if (r == null) {
            throw new IllegalArgumentException("Right " + rightName + " not found");
        }
        AppProfileRight pr = pu.createQuery("Select u from AppProfileRight u where u.profileId=:profileId and u.rightName=:rightName")
                .setParameter("rightName", rightName)
                .setParameter("profileId", profileId)
                .getEntity();
        if (pr == null) {
            pr = new AppProfileRight();
            pr.setProfile(p);
            pr.setRight(r);
            pu.persist(pr);
            return true;
        }
        return false;
    }

    public boolean userAddProfile(int userId, String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        if (pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId and u.profile.name=:name")
                .setParameter("userId", userId)
                .setParameter("name", profileName)
                .isEmpty()) {
            AppUser u = findUser(userId);
            if (u == null) {
                throw new IllegalArgumentException("Unknown User " + userId);
            }
            AppProfile p = findProfileByName(profileName);
            if (p == null) {
                p = new AppProfile();
                p.setName(profileName);
                pu.persist(p);
            }

            pu.persist(new AppUserProfileBinding(u, p));
            return true;
        }
        return false;
    }

    public boolean userHasProfile(int userId, String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return !pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId and u.profile.name=:name")
                .setParameter("userId", userId)
                .setParameter("name", profileName)
                .isEmpty();
    }

    public Set<String> findUserRights(int userId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        List<String> rights = pu.createQuery("Select n.rightName from AppUserProfileBinding  u "
                + " inner join AppProfileRight n on n.profileId=u.profileId "
                + " where u.userId=:userId")
                .setParameter("userId", userId)
                .getValueList(0);
        return new HashSet<>(rights);
    }

    public AppProfile findOrCreateProfile(String profileName) {
        AppProfile p = findProfileByName(profileName);
        if (p == null) {
            p = new AppProfile();
            p.setName(profileName);
            UPA.getPersistenceUnit().persist(p);
        }
        return p;
    }

    public AppProfile findProfileByName(String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQueryBuilder(AppProfile.class).addAndField("name", profileName)
                .getEntity();

    }

    public List<AppProfile> findProfilesByUser(int userId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId")
                .setParameter("userId", userId)
                .getEntityList();
    }

    public List<AppUser> findUsersByProfile(int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.user from AppUserProfileBinding  u where u.profileId=:profileId")
                .setParameter("profileId", profileId)
                .getEntityList();
    }

    public List<AppUser> findUsersByProfile(String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.user from AppUserProfileBinding  u where u.profile.name=:profileName")
                .setParameter("profileName", profileName)
                .getEntityList();
    }

    public List<AppProfile> findProfiles() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u from AppProfile u")
                .getEntityList();
    }

    public List<AppUser> findUsers() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u from AppUser  u")
                .getEntityList();
    }

    public List<AppUser> findEnabledUsers() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u from AppUser  u where u.enabled=true and u.deleted=false")
                .getEntityList();
    }

    public AppUser findUser(String login, String password) {
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

    public AppDepartment findDepartment(String code) {
        return UPA.getPersistenceUnit().
                createQuery("Select a from AppDepartment a where a.code=:code or a.name=:code or a.name2=:code")
                .setParameter("code", code)
                .getEntity();
    }

    public AppCivility findCivility(String t) {
        return (AppCivility) UPA.getPersistenceUnit().findByMainField(AppCivility.class, t);
    }

    public AppGender findGender(String t) {
        return (AppGender) UPA.getPersistenceUnit().findByMainField(AppGender.class, t);
    }

    public List<AppGender> findGenders() {
        List<AppGender> all = UPA.getPersistenceUnit().findAll(AppGender.class);
        return all;
    }

    private static class InitData {

        long now;
        AppProfile adminProfile;
        AppUser admin;
        AppUserType adminType;
        List<AppCivility> civilities;
        List<AppGender> genders;
        List<AppDepartment> departments;
    }

    @Install
    public void installService() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppCountry tunisia = pu.findByMainField(AppCountry.class, "Tunisie");
        if (tunisia == null) {
            tunisia=new AppCountry();
            tunisia.setName("Tunisie");
            pu.persist(tunisia);
        }
        AppIndustry eduIndustry = pu.findByMainField(AppIndustry.class, "Education");
        if (eduIndustry == null) {
            eduIndustry=new AppIndustry();
            eduIndustry.setName("Education");
            pu.persist(eduIndustry);
        }

        AppCompany eniso = pu.findByMainField(AppIndustry.class, "ENISo");
        if (eniso == null) {
            eniso=new AppCompany();
            eniso.setName("ENISo");
            eniso.setIndustry(eduIndustry);
            eniso.setCountry(tunisia);
            pu.persist(eniso);
        }

        AppProfile oldAdmin = pu.findByMainField(AppProfile.class, "admin");
        if (oldAdmin != null) {
            oldAdmin.setName("Admin");
            pu.merge(oldAdmin);
        }
        InitData d = new InitData();
        d.now = System.currentTimeMillis();
        d.adminProfile = new AppProfile();
        d.adminProfile.setName("Admin");
        d.adminProfile = insertIfNotFound(d.adminProfile);

        d.adminType = new AppUserType();
        d.adminType.setName("Admin");
        d.adminType = insertIfNotFound(d.adminType);

        d.civilities = new ArrayList<>();
        for (String n : new String[]{"M.", "Mlle", "Mme"}) {
            AppCivility c = new AppCivility(0, n);
            c = insertIfNotFound(c);
            d.civilities.add(c);
        }
        d.genders = new ArrayList<>();
        for (String n : new String[]{"H", "F"}) {
            AppGender c = new AppGender(0, n);
            c = insertIfNotFound(c);
            d.genders.add(c);
        }

        AppUser uu = new AppUser();
        d.admin = uu;
        d.admin.setFirstName("admin");
        d.admin.setLastName("admin");
        d.admin.setFullName("admin");
        d.admin.setLogin("admin");
        d.admin.setPassword("admin");
        d.admin.setCivitity(d.civilities.get(0));
        d.admin.setGender(d.genders.get(0));
        d.admin.setEmail("admin@vr.net");
        d.admin.setType(d.adminType);
        d.admin = insertIfNotFound(d.admin);
        if (d.admin == uu) {
            pu.persist(new AppUserProfileBinding(d.admin, d.adminProfile));
        }
        d.departments = new ArrayList<>();
        for (String[] n : new String[][]{{"II", "Informatique Industrielle"}, {"EI", "Electronique Indstrielle"}, {"MA", "Mecanique Avancee"}, {"ADM", "Administration"}}) {
            AppDepartment c = new AppDepartment();
            c.setCode(n[0]);
            c.setName(n[1]);
            AppDepartment old = pu.findByField(AppDepartment.class, "code", c.getCode());
            if (old == null) {
                c = insertIfNotFound(c);
            } else {
                c = old;
            }
            d.departments.add(c);
        }
        for (Entity entity : pu.getEntities()) {
            if (!entity.isSystem()) {
                EntityShield s = entity.getShield();
                if (true) {
                    AppRightName r = new AppRightName();
                    r.setName(entity.getAbsoluteName() + ".DefaultEditor");
                    r.setDescription("List " + entity.getName());
                    insertIfNotFound(r);
                }
                if (true) {
                    AppRightName r = new AppRightName();
                    r.setName(entity.getAbsoluteName() + ".Navigate");
                    r.setDescription("List " + entity.getName());
                    insertIfNotFound(r);
                }
                if (true) {
                    AppRightName r = new AppRightName();
                    r.setName(entity.getAbsoluteName() + ".Load");
                    r.setDescription("Detail " + entity.getName());
                    insertIfNotFound(r);
                }
                if (!ADMIN_ENTITIES.contains(entity.getName())) {
                    if (s.isPersistSupported()) {
                        AppRightName r = new AppRightName();
                        r.setName(entity.getAbsoluteName() + ".Persist");
                        r.setDescription("Persist " + entity.getName());
                        insertIfNotFound(r);
                    }
                    if (s.isUpdateSupported()) {
                        AppRightName r = new AppRightName();
                        r.setName(entity.getAbsoluteName() + ".Update");
                        r.setDescription("Update " + entity.getName());
                        insertIfNotFound(r);
                    }
                    if (s.isDeleteSupported()) {
                        AppRightName r = new AppRightName();
                        r.setName(entity.getAbsoluteName() + ".Remove");
                        r.setDescription("Remove " + entity.getName());
                        insertIfNotFound(r);
                    }
                    String extraActions = entity.getProperties().getString("actions");
                    if (extraActions != null) {
                        for (String a : extraActions.split(" ,|;")) {
                            if (a.length() > 0) {
                                AppRightName r = new AppRightName();
                                r.setName(entity.getAbsoluteName() + "." + a);
                                r.setDescription(a + " " + entity.getName());
                                insertIfNotFound(r);
                            }
                        }
                    }
                }
            }
        }
    }

    public <T> T insertIfNotFound(T o) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        Entity e = pu.getEntity(o.getClass());
        return insertIfNotFound(o, e.getMainField().getName());
    }

    public <T> T insertIfNotFound(T o, String field) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        Entity e = pu.getEntity(o.getClass());
        Object value = e.getBuilder().entityToRecord(o, true).getObject(field);
        T t = pu.createQueryBuilder(o.getClass()).setExpression(new Equals(new Var(field), new Literal(value, e.getField(field).getDataType())))
                .getEntity();
        if (t == null) {
            pu.persist(o);
            return o;
        }
        return t;
    }

    public <T> List<T> filterByProfilePattern(List<T> in, Integer userId, String login, ProfilePatternFilter<T> filter) {
        List<T> out = new ArrayList<>();
        final HashMap<String, Object> cache = new HashMap<String, Object>();
        if (in != null) {
            for (T i : in) {
                if (userMatchesProfileFilter(userId, login, new ProfileFilterExpression(filter.getProfilePattern(i)), cache)) {
                    out.add(i);
                }
            }
        }
        return out;
    }

    private InSetEvaluator createProfilesEvaluator(final Set<String> profiles) {
        return new InSetEvaluator(profiles);
    }

    public boolean userMatchesProfileFilter(Integer userId, String login, String profile, String whereClause) {
        return userMatchesProfileFilter(userId, login, profile, whereClause, null);
    }

    private boolean userMatchesProfileFilter(Integer userId, String login, String profile, String whereClause, Map<String, Object> cache) {
        return userMatchesProfileFilter(userId, login, new ProfileFilterExpression(profile, whereClause), cache);
    }

    private boolean userMatchesProfileFilter(Integer userId, String login, ProfileFilterExpression profileExpr, Map<String, Object> cache) {
        if (Strings.isNullOrEmpty(profileExpr.getFilterExpression()) && Strings.isNullOrEmpty(profileExpr.getProfileListExpression())) {
            return true;
        }
        if (cache == null) {
            cache = new HashMap<>();
        }
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userId != null) {
            Map<Integer, AppUser> usersById = (Map<Integer, AppUser>) cache.get("usersById");
            if (usersById == null) {
                usersById = new HashMap<>();
                cache.put("usersById", usersById);
            }
            u = usersById.get(userId);
            if (u == null) {
                u = findUser(userId);
                if (u != null) {
                    usersById.put(userId, u);
                }
            }
        }
        if (!Strings.isNullOrEmpty(login)) {
            Map<String, AppUser> usersByLogin = (Map<String, AppUser>) cache.get("usersByLogin");
            if (usersByLogin == null) {
                usersByLogin = new HashMap<>();
                cache.put("usersByLogin", usersByLogin);
            }

            AppUser u1 = null;
            u1 = usersByLogin.get(login);
            if (u1 == null) {
                u1 = findUser(login);
                if (u1 != null) {
                    usersByLogin.put(login, u1);
                }
            }
            if (u != null && u1 != null && u.getId() != u1.getId()) {
                u = null;
            } else if (u == null) {
                u = u1;
            }
        }

        Map<Integer, Set<String>> usersProfilesByUserId = (Map<Integer, Set<String>>) cache.get("usersProfilesByUserId");
        if (usersProfilesByUserId == null) {
            usersProfilesByUserId = new HashMap<>();
            cache.put("usersProfilesByUserId", usersProfilesByUserId);
        }
        Set<String> foundProfileNames = null;
        if (u != null) {
            foundProfileNames = (Set<String>) usersProfilesByUserId.get(u.getId());
            if (foundProfileNames == null) {
                foundProfileNames = new HashSet<>();
                for (AppProfile p : findProfilesByUser(u.getId())) {
                    foundProfileNames.add(p.getName().toLowerCase());
                }
                foundProfileNames.add(u.getLogin().toLowerCase());
                usersProfilesByUserId.put(u.getId(), foundProfileNames);
            }
        } else {
            foundProfileNames = new HashSet<>();
        }
        InSetEvaluator evaluator = createProfilesEvaluator(foundProfileNames);
        boolean b = evaluator.evaluate(profileExpr.getProfileListExpression());
        if (b && !Strings.isNullOrEmpty(profileExpr.getFilterExpression())) {
            return filterUsersByExpression(Arrays.asList(u), profileExpr.getFilterExpression()).size() > 0;
        }
        return b;
    }

//    public List<AppProfile> resolveProfilesByProfileFilter(String profile) {
//        PersistenceUnit pu = UPA.getPersistenceUnit();
//        if (profile != null && profile.trim().length() > 0) {
//            StringBuilder x = new StringBuilder();
//            for (String p : profile.split(" , |;")) {
//                if (p != null) {
//                    x.append("/").append(p);
//                }
//            }
//            x.append("/");
//            return pu.createQuery("Select u from AppProfile u where :expr like concat('%/',u.name,'/%')")
//                    .setParameter("expr", x.toString())
//                    .getEntityList();
//        }
//        return Collections.EMPTY_LIST;
//    }
    public List<AppUser> resolveUsersByProfileFilter(String profilePattern) {
        //check if pattern contains where clause!
        ProfileFilterExpression ee = new ProfileFilterExpression(profilePattern);
        ProfileFilterExpression profilesOnlyExpr = new ProfileFilterExpression(ee.getProfileListExpression(), null);

        List<AppUser> all = new ArrayList<>();
        final HashMap<String, Object> cache = new HashMap<String, Object>();
        List<AppUser> users = findEnabledUsers();
        HashMap<Integer, AppUser> usersById = new HashMap<Integer, AppUser>();
        HashMap<String, AppUser> usersByLogin = new HashMap<String, AppUser>();
        for (AppUser user : users) {
            usersById.put(user.getId(), user);
            usersByLogin.put(user.getLogin(), user);
        }
        cache.put("usersById", usersById);
        cache.put("usersByLogin", usersByLogin);
        for (AppUser u : users) {
            if (userMatchesProfileFilter(u.getId(), u.getLogin(), profilesOnlyExpr, cache)) {
                all.add(u);
            }
        }
        return filterUsersByExpression(all, ee.getFilterExpression());
    }

    private List<AppUser> filterUsersByExpression(List<AppUser> all, String expression) {
        if (Strings.isNullOrEmpty(expression)) {
            return new ArrayList<AppUser>(all);
        }
        HashSet<Integer> ids = new HashSet<>();
        for (AppUser i : all) {
            ids.add(i.getId());
        }
        return UPA.getPersistenceUnit()
                .createQuery("Select x from AppUser x where " + expression)
                .getEntityList();
    }
//    public List<AppUser> resolveUsersByProfileFilter(String profile) {
//        PersistenceUnit pu = UPA.getPersistenceUnit();
//        if (profile != null && profile.trim().length() > 0) {
//            StringBuilder x = new StringBuilder();
//            for (String p : profile.split(" , |;")) {
//                if (p != null) {
//                    x.append("/").append(p);
//                }
//            }
//            return pu.createQuery("Select u.user from AppUserProfileBinding u where :expr like concat('/',u.profile.name,'/')")
//                    .setParameter("expr", x.toString())
//                    .getEntityList();
//        }
//        return Collections.EMPTY_LIST;
//    }

    public void setAppProperty(String propertyName, String userLogin, Object propertyValue) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userLogin != null) {
            u = findUser(userLogin);
        }
        AppProperty ap = new AppProperty();
        ap.setEnabled(true);
        ap.setUser(u);
        ap.setPropertyName(propertyName);
        String propertyType = "null";
        String propertyValueString = "";
        if (propertyValue != null) {
            if (propertyValue instanceof String) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "string";
            } else if (propertyValue instanceof Integer) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "int";
            } else if (propertyValue instanceof Long) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "long";
            } else if (propertyValue instanceof Double) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "double";
            } else if (propertyValue instanceof Boolean) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "boolean";
            } else {
                throw new IllegalArgumentException("Not supported " + propertyValue);
            }
        }
        ap.setPropertyType(propertyType);
        ap.setPropertyValue(propertyValueString);
        setAppProperty(ap);
    }

    public void setEnabledAppProperty(String propertyName, String userLogin, boolean enabled) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userLogin != null) {
            u = findUser(userLogin);
        }
        AppProperty ap = new AppProperty();
        ap.setEnabled(true);
        ap.setUser(u);
        ap.setPropertyName(propertyName);
        String propertyType = "null";
        String propertyValueString = "";
        ap.setPropertyType(propertyType);
        ap.setPropertyValue(propertyValueString);
        ap.setEnabled(enabled);
        setAppProperty(ap);
    }

    public Object getAppPropertyValue(AppProperty p) {
        if (p == null) {
            return null;
        }
        String t = p.getPropertyType();
        String v = p.getPropertyValue();
        if ("null".equals(t)) {
            return null;
        }
        if ("string".equals(t)) {
            return v;
        }
        if ("int".equals(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Integer.valueOf(v);
        }
        if ("long".equals(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Long.valueOf(v);
        }
        if ("double".equals(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Double.valueOf(v);
        }
        if ("boolean".equals(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Boolean.valueOf(v);
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public Object getOrCreateAppPropertyValue(String propertyName, String userLogin, Object value) {
        AppProperty p = getAppProperty(propertyName, userLogin);
        if (p != null) {
            if (p.isEnabled()) {
                Object v = getAppPropertyValue(p);
                if (v != null) {
                    return v;
                }
            }
        }
        if (value != null) {
            setAppProperty(propertyName, userLogin, value);
        }
        return value;
    }

    public Object getAppPropertyValue(String propertyName, String userLogin) {
        AppProperty p = getAppProperty(propertyName, userLogin);
        if (p != null && p.isEnabled()) {
            return getAppPropertyValue(p);
        }
        return null;
    }

    public AppProperty getAppProperty(String propertyName, String userLogin) {

        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userLogin != null) {
            u = findUser(userLogin);
            if (u == null) {
                return null;
            }
        }

        QueryBuilder q = pu.createQueryBuilder(AppProperty.class);
        q.addAndField("propertyName", propertyName);
        if (u != null) {
            q.addAndExpression("(userId=" + u.getId() + " or userId = null)");
        } else {
            q.addAndExpression("(userId = null)");
        }
        List<AppProperty> props = q.getEntityList();
        List<AppProperty> all = new ArrayList<AppProperty>(props);
        Collections.sort(all, new Comparator<AppProperty>() {

            @Override
            public int compare(AppProperty o1, AppProperty o2) {
                AppUser u1 = o1.getUser();
                AppUser u2 = o2.getUser();
                if (u1 == null && u2 != null) {
                    return 1;

                } else if (u1 != null && u2 == null) {
                    return -1;
                }
                String s1 = o1.getPropertyName();
                String s2 = o2.getPropertyName();
                if (s1 == null) {
                    s1 = "";
                }
                if (s2 == null) {
                    s2 = "";
                }
                int x = s1.compareTo(s2);
                if (x != 0) {
                    return x;
                }
                x = s1.compareTo(s2);
                if (x != 0) {
                    return x;
                }
                return 0;
            }
        });
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    public void setAppProperty(AppProperty ap) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppProperty old = pu.createQueryBuilder(AppProperty.class)
                .addAndField("propertyName", ap.getPropertyName())
                .getEntity();
        if (old == null) {
            pu.persist(ap);
        } else {
            old.setEnabled(ap.isEnabled());
            old.setPropertyType(ap.getPropertyType());
            old.setPropertyValue(ap.getPropertyValue());
            pu.merge(old);
        }
    }
}