/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.teachereval.web;

import net.vpc.app.vainruling.core.service.CorePlugin;
import net.vpc.app.vainruling.core.web.OnPageLoad;
import net.vpc.app.vainruling.core.web.UCtrl;
import net.vpc.app.vainruling.core.web.UPathItem;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.AcademicPerfEvalPlugin;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.model.AcademicFeedback;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.model.AcademicFeedbackGroup;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.model.AcademicFeedbackQuestion;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.model.AcademicFeedbackResponse;
import net.vpc.app.vainruling.plugins.academic.service.AcademicPlugin;
import net.vpc.app.vainruling.plugins.academic.service.model.config.AcademicStudent;
import net.vpc.common.jsf.FacesUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.upa.UPA;
import org.primefaces.component.slider.Slider;
import org.primefaces.event.SlideEndEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.model.SelectItem;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author taha.bensalah@gmail.com
 */
@UCtrl(
        breadcrumb = {
                @UPathItem(title = "Education", css = "fa-dashboard", ctrl = "")},
//        css = "fa-table",
//        title = "Fiches Eval. enseignements",
        menu = "/Education/Evaluation",
        url = "modules/academic/perfeval/student-feedback",
        securityKey = "Custom.Academic.StudentFeedback"
)
public class StudentFeedbackCtrl {

    private static final Logger log = Logger.getLogger(StudentFeedbackCtrl.class.getName());
    @Autowired
    private CorePlugin core;
    @Autowired
    private AcademicPlugin academic;
    @Autowired
    private AcademicPerfEvalPlugin feedback;
    private Model model = new Model();

    @OnPageLoad
    public void onLoad() {
        onReloadFeedbacks();
    }

    public void onSlideEnd(SlideEndEvent event) {
        Slider s=(Slider) event.getSource();
        for (UIComponent uiComponent : s.getParent().getChildren()) {
            if(uiComponent.getId().equals("responseId")){
                UIInput input=(UIInput) uiComponent;
                Object value = input.getValue();
                int id = Integer.parseInt(""+value);
                for (Row row : getModel().getRows()) {
                    for (Question question : row.getQuestions()) {
                        AcademicFeedbackResponse response = question.getResponse();
                        if(response.getId()== id){
                            response.setResponse(""+event.getValue());
                            return;
                        }
                    }
                }
                //uiComponent.get
            }
        }
    }

    public void onReloadFeedbacks() {
        AcademicStudent s = academic.getCurrentStudent();
        getModel().setFeedbacks(new ArrayList<SelectItem>());
        if (s != null) {
            HashSet<String> ids = new HashSet<>();
            for (AcademicFeedback f : feedback.findStudentFeedbacks(core.getCurrentPeriod().getId(),s.getId(), false, false, true)) {
                getModel().getFeedbacks().add(new SelectItem(String.valueOf(f.getId()), f.getCourse().getFullName() + " - " + academic.getValidName(f.getCourse().getTeacher())));
                ids.add(String.valueOf(f.getId()));
            }
            if (!ids.contains(getModel().getSelectedFeedback())) {
                getModel().setSelectedFeedback(null);
                for (String id : ids) {
                    getModel().setSelectedFeedback(id);
                    break;
                }
            }
        }
        onFeedbackChange();
    }

    public void onFeedbackChange() {
        List<Row> rows = new ArrayList<>();
        if (!StringUtils.isEmpty(getModel().getSelectedFeedback())) {
            getModel().setFeedback(feedback.findFeedback(Integer.parseInt(getModel().getSelectedFeedback())));
            List<AcademicFeedbackGroup> groups = feedback.findStudentFeedbackGroups(getModel().getFeedback().getModel().getId());
            Map<Integer, Question> questionsMap = new HashMap<>();
            Map<Integer, Row> groupsMap = new HashMap<>();
            for (AcademicFeedbackGroup group : groups) {
                Row row = new Row();
                row.setTitle(group.getName());
                ArrayList<Question> questions = new ArrayList<Question>();
                row.setQuestions(questions);
                groupsMap.put(group.getId(), row);
                rows.add(row);
            }

            for (AcademicFeedbackQuestion r : feedback.findStudentFeedbackQuestionsByModel(getModel().getFeedback().getModel().getId())) {
                Question q = new Question();
                questionsMap.put(r.getId(), q);
                Row gg = groupsMap.get(r.getParent().getId());
                if (gg != null) {
                    gg.getQuestions().add(q);
                }
            }
            for (AcademicFeedbackResponse r : feedback.findStudentFeedbackResponses(Integer.parseInt(getModel().getSelectedFeedback()))) {
                Question qq = questionsMap.get(r.getQuestion().getId());
                if (qq != null) {
                    qq.setResponse(r);
                }
            }

        }
        getModel().setRows(rows);
    }

    public void onValidate() {
        onSave();
    }

    public void onSave() {
        for (Row row : getModel().getRows()) {
            for (Question question : row.getQuestions()) {
                feedback.saveResponse(question.getResponse());
            }
        }
        FacesUtils.addInfoMessage("Formulaire enregistré");
    }

    public void onSaveAndValidate() {
        onSave();
        boolean allvalid = true;
        for (Row row : getModel().getRows()) {
            for (Question question : row.getQuestions()) {
                if (StringUtils.isEmpty(question.getResponse().getResponse())) {
                    allvalid = false;
                }
            }
        }
        if (allvalid) {
            getModel().getFeedback().setValidated(true);
            UPA.getPersistenceUnit().merge(getModel().getFeedback());
            getModel().setSelectedFeedback(null);
            getModel().setFeedback(null);
            onReloadFeedbacks();
            FacesUtils.addInfoMessage("Formulaire validé");
            if(getModel().getFeedbacks().isEmpty()){
                FacesUtils.addInfoMessage("Merci beaucoup pour votre implication. Toutes les fiches sont validées");
            }
        } else {
            FacesUtils.addErrorMessage("Merci de repondre à toutes les questions avant de valider");
        }
    }

    public Model getModel() {
        return model;
    }

    public static class Question {

        AcademicFeedbackResponse response;

        public AcademicFeedbackResponse getResponse() {
            return response;
        }

        public void setResponse(AcademicFeedbackResponse response) {
            this.response = response;
        }

    }

    public static class Row {

        private String title;
        private List<Question> questions;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public void setQuestions(List<Question> questions) {
            this.questions = questions;
        }

    }

    public static class Model {

        private String title;
        private String selectedFeedback;
        private List<SelectItem> feedbacks = new ArrayList<SelectItem>();
        private AcademicFeedback feedback = null;
        private List<Row> rows = new ArrayList<>();

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<SelectItem> getFeedbacks() {
            return feedbacks;
        }

        public void setFeedbacks(List<SelectItem> feedbacks) {
            this.feedbacks = feedbacks;
        }

        public String getSelectedFeedback() {
            return selectedFeedback;
        }

        public void setSelectedFeedback(String selectedFeedback) {
            this.selectedFeedback = selectedFeedback;
        }

        public List<Row> getRows() {
            return rows;
        }

        public void setRows(List<Row> rows) {
            this.rows = rows;
        }

        public AcademicFeedback getFeedback() {
            return feedback;
        }

        public void setFeedback(AcademicFeedback feedback) {
            this.feedback = feedback;
        }

    }

}
