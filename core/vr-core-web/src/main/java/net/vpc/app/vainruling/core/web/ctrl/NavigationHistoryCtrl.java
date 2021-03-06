/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.web.ctrl;

import net.vpc.app.vainruling.core.service.CorePlugin;
import net.vpc.app.vainruling.core.service.VrApp;
import net.vpc.app.vainruling.core.service.model.AppDepartment;
import net.vpc.app.vainruling.core.service.model.AppUser;
import net.vpc.app.vainruling.core.service.model.AppUserType;
import net.vpc.app.vainruling.core.service.notification.PollAware;
import net.vpc.app.vainruling.core.service.security.UserSession;
import net.vpc.app.vainruling.core.web.OnPageLoad;
import net.vpc.app.vainruling.core.web.PageInfo;
import net.vpc.app.vainruling.core.web.UCtrl;
import net.vpc.app.vainruling.core.web.menu.VrMenuManager;
import net.vpc.app.vainruling.core.web.util.ChartUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Chronometer;
import org.primefaces.model.chart.DonutChartModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author taha.bensalah@gmail.com
 */
@UCtrl(
//        title = "Historique de Navigation",
        url = "modules/util/navigation-history",
        menu = "/",
        securityKey = "Custom.Util.NavigationHistory"
)
@Scope(value = "session")
public class NavigationHistoryCtrl  {
    public static final Date MIN_DATE = new Date(Long.MIN_VALUE);
    private static final Comparator<UserSession> SESSION_COMPARATOR = new Comparator<UserSession>() {
        @Override
        public int compare(UserSession o1, UserSession o2) {
            Date d1=(o1!=null && o1.getConnexionTime()!=null)?o1.getConnexionTime(): MIN_DATE;
            Date d2=(o2!=null && o2.getConnexionTime()!=null)?o2.getConnexionTime(): MIN_DATE;
            return -d1.compareTo(d2);
        }
    };

    @Autowired
    private CorePlugin core;
    private final Model model = new Model();


    @OnPageLoad
    public void onRefresh() {
        ArrayList<PageInfo> historyElements = new ArrayList<>(VrApp.getBean(VrMenuManager.class).getPageHistory());
        Collections.reverse(historyElements);
        model.setHistoryElements(historyElements);
    }

    public Model getModel() {
        return model;
    }

    public void onPoll() {
        onUpdate();
    }

    public void onUpdate() {
        onRefresh();
    }


    public static class Model {

        private List<PageInfo> historyElements = new ArrayList<>();

        public List<PageInfo> getHistoryElements() {
            return historyElements;
        }

        public void setHistoryElements(List<PageInfo> historyElements) {
            this.historyElements = historyElements;
        }
    }
}
