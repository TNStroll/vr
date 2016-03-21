/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.teachereval.web.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import net.vpc.app.vainruling.api.CorePlugin;
import net.vpc.app.vainruling.api.VrApp;
import net.vpc.app.vainruling.api.util.VrHelper;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.AcademicPerfEvalPlugin;
import net.vpc.common.strings.StringUtils;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author vpc
 */
@Component
@ManagedBean
@Scope("session")
public class GenerateFeedbackActionCtrl {

    private static final Logger log = Logger.getLogger(GenerateFeedbackActionCtrl.class.getName());
    @Autowired
    private CorePlugin core;
    private Model model = new Model();

    public static class Config {

        private String type;
        private int modelId;
        private String title;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getModelId() {
            return modelId;
        }

        public void setModelId(int modelId) {
            this.modelId = modelId;
        }

    }

    public void openDialog(String config) {
        openDialog(VrHelper.parseJSONObject(config, Config.class));
    }

    public void openDialog(Config config) {
        if (config == null) {
            config = new Config();
        }
        getModel().setFilter("");

        String t = config.getTitle();

        getModel().setTitle(StringUtils.isEmpty(t) ? "Générer Feedbacks" : t);
        getModel().setModelId(config.getModelId());

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);

        RequestContext.getCurrentInstance().openDialog("/modules/academic/perfeval/generateFeedbackdialog", options, null);

    }

    public void onUpdate() {
//        System.out.println("on update SendExternalMailActionCtrl");
    }

    public void onChange() {

    }

    public void fireEventExtraDialogApply() {
        VrApp.getBean(AcademicPerfEvalPlugin.class).generatedStudentsFeedbackForm(
                getModel().getModelId(),
                getModel().getFilter()
        );
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void fireEventExtraDialogCancel() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public static class Model {

        private String title;
        private String filter;
        private int modelId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public int getModelId() {
            return modelId;
        }

        public void setModelId(int modelId) {
            this.modelId = modelId;
        }

    }

    public Model getModel() {
        return model;
    }

}