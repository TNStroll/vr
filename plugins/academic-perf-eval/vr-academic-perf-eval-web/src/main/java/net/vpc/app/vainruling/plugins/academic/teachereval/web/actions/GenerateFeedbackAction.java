/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.teachereval.web.actions;

import java.util.List;
import net.vpc.app.vainruling.api.EntityAction;
import net.vpc.app.vainruling.api.VrApp;
import net.vpc.app.vainruling.api.web.ctrl.EditCtrlMode;
import net.vpc.app.vainruling.api.web.obj.ActionDialog;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.AcademicPerfEvalPlugin;
import net.vpc.app.vainruling.plugins.academic.perfeval.service.model.AcademicFeedbackModel;

/**
 *
 * @author vpc
 */
@EntityAction(entityType = AcademicFeedbackModel.class,
        actionName = "GenerateFeedback",
        actionLabel = "Generate", actionStyle = "fa-envelope-o",
        dialog = true
)
public class GenerateFeedbackAction implements ActionDialog {

    @Override
    public void openDialog(String actionId, List<String> itemIds) {
        GenerateFeedbackActionCtrl.Config c = new GenerateFeedbackActionCtrl.Config();
        c.setModelId(Integer.parseInt(itemIds.get(0)));
        VrApp.getBean(GenerateFeedbackActionCtrl.class).openDialog(c);
    }

    @Override
    public boolean isEnabled(Class entityType, EditCtrlMode mode, Object value) {
        return value != null;
    }

    @Override
    public void invoke(Class entityType, Object obj, Object[] args) {
        VrApp.getBean(AcademicPerfEvalPlugin.class).generatedStudentsFeedbackForm(((AcademicFeedbackModel) obj).getId(), 
                (String) args[0]);
    }

}
