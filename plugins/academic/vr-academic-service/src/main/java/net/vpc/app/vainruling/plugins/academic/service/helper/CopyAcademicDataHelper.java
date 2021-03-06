package net.vpc.app.vainruling.plugins.academic.service.helper;

import net.vpc.app.vainruling.core.service.CorePlugin;
import net.vpc.app.vainruling.core.service.VrApp;
import net.vpc.app.vainruling.core.service.model.AppPeriod;
import net.vpc.app.vainruling.plugins.academic.service.AcademicPlugin;
import net.vpc.app.vainruling.plugins.academic.service.model.config.AcademicTeacher;
import net.vpc.app.vainruling.plugins.academic.service.model.current.*;
import net.vpc.common.util.Converter;
import net.vpc.common.util.DefaultMapList;
import net.vpc.common.util.MapList;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.UPA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by vpc on 6/25/16.
 */
public class CopyAcademicDataHelper {
    public static final Logger log = Logger.getLogger(CopyAcademicDataHelper.class.getName());

    private Map<Integer, AcademicTeacherSemestrialLoad> _copyAcademicDataAcademicTeacherSemestrialLoad(int fromPeriodId, AppPeriod toPeriod, PersistenceUnit pu) {
        final AcademicPlugin p = VrApp.getBean(AcademicPlugin.class);
        Map<Integer, AcademicTeacherSemestrialLoad> oldIdToNewObject = new HashMap<>();
//copy AcademicTeacherSemestrialLoad
        Converter<String, AcademicTeacherSemestrialLoad> converter = new Converter<String, AcademicTeacherSemestrialLoad>() {
            @Override
            public String convert(AcademicTeacherSemestrialLoad value) {
                return value.getTeacher().getId() + "-" + value.getSemester();
            }
        };
        MapList<String, AcademicTeacherSemestrialLoad> fromList = new DefaultMapList<String, AcademicTeacherSemestrialLoad>(
                p.findTeacherSemestrialLoadsByPeriod(fromPeriodId),
                converter
        );

        MapList<String, AcademicTeacherSemestrialLoad> toList = new DefaultMapList<String, AcademicTeacherSemestrialLoad>(
                p.findTeacherSemestrialLoadsByPeriod(toPeriod.getId()),
                converter
        );
        for (AcademicTeacherSemestrialLoad v : fromList) {
            if (!toList.containsMappedValue(v)) {
                AcademicTeacherSemestrialLoad v2 = pu.copyObject(v);
                v2.setId(0);
                v2.setPeriod(toPeriod);
                pu.persist(v2);
                oldIdToNewObject.put(v.getId(), v2);
            } else {
                oldIdToNewObject.put(v.getId(), toList.getByValue(v));
            }
        }
        return oldIdToNewObject;
    }

    private Map<Integer, AcademicCoursePlan> _copyAcademicDataAcademicCoursePlan(int fromPeriodId, AppPeriod toPeriod, Map<Integer, AcademicCourseGroup> groupsByOlId, PersistenceUnit pu) {
        final AcademicPlugin p = VrApp.getBean(AcademicPlugin.class);
        Map<Integer, AcademicCoursePlan> oldIdToNewObject = new HashMap<>();
        Converter<String, AcademicCoursePlan> converter = new Converter<String, AcademicCoursePlan>() {
            @Override
            public String convert(AcademicCoursePlan value) {
                return value.getFullName();
            }
        };
        MapList<String, AcademicCoursePlan> fromList = new DefaultMapList<>(
                p.findCoursePlans(fromPeriodId),
                converter
        );

        MapList<String, AcademicCoursePlan> toList = new DefaultMapList<>(
                p.findCoursePlans(toPeriod.getId()),
                converter
        );
        for (AcademicCoursePlan v : fromList) {
            if (!toList.containsMappedValue(v)) {
                AcademicCoursePlan v2 = pu.copyObject(v);
                v2.setId(0);
                v2.setPeriod(toPeriod);
                v2.setCourseGroup(v2.getCourseGroup() == null ? null : groupsByOlId.get(v2.getCourseGroup().getId()));
                pu.persist(v2);
                oldIdToNewObject.put(v.getId(), v2);
            } else {
                oldIdToNewObject.put(v.getId(), toList.getByValue(v));
            }
        }
        return oldIdToNewObject;
    }

    private Map<Integer, AcademicCourseGroup> _copyAcademicDataAcademicCourseGroup(int fromPeriodId, AppPeriod toPeriod, PersistenceUnit pu) {
        final AcademicPlugin p = VrApp.getBean(AcademicPlugin.class);
        Map<Integer, AcademicCourseGroup> oldIdToNewObject = new HashMap<>();
        Converter<String, AcademicCourseGroup> converter = new Converter<String, AcademicCourseGroup>() {
            @Override
            public String convert(AcademicCourseGroup value) {
                return value.getAcademicClass().getName() + "-" + value.getName();
            }
        };
        MapList<String, AcademicCourseGroup> fromList = new DefaultMapList<>(
                p.findCourseGroups(fromPeriodId),
                converter
        );

        MapList<String, AcademicCourseGroup> toList = new DefaultMapList<>(
                p.findCourseGroups(toPeriod.getId()),
                converter
        );
        for (AcademicCourseGroup v : fromList) {
            if (!toList.containsMappedValue(v)) {
                AcademicCourseGroup v2 = pu.copyObject(v);
                v2.setId(0);
                v2.setPeriod(toPeriod);
                pu.persist(v2);
                oldIdToNewObject.put(v.getId(), v2);
            } else {
                oldIdToNewObject.put(v.getId(), toList.getByValue(v));
            }
        }
        return oldIdToNewObject;
    }

    private Map<Integer, AcademicCourseAssignment> _copyAcademicDataAcademicCourseAssignment(int fromPeriodId, AppPeriod toPeriod, Map<Integer, AcademicCoursePlan> coursePlanMap, PersistenceUnit pu) {
        final AcademicPlugin p = VrApp.getBean(AcademicPlugin.class);
        Map<Integer, AcademicCourseAssignment> oldIdToNewObject = new HashMap<>();
        Converter<String, AcademicCourseAssignment> converter = new Converter<String, AcademicCourseAssignment>() {
            @Override
            public String convert(AcademicCourseAssignment value) {
                return value.getFullName();
            }
        };
        List<AcademicCourseAssignment> courseAssignments = p.findCourseAssignments(fromPeriodId);
        MapList<String, AcademicCourseAssignment> fromList = new DefaultMapList<>(
                courseAssignments,
                converter
        );

        MapList<String, AcademicCourseAssignment> toList = new DefaultMapList<>(
                p.findCourseAssignments(toPeriod.getId()),
                converter
        );
        if (fromList.size() != courseAssignments.size()) {
            HashMap<String, AcademicCourseAssignment> visited = new HashMap<>();
            HashSet<String> reported = new HashSet<>();
            log.severe("Some Course Assignments have similar names. Here are the problems");
            for (AcademicCourseAssignment assignment : courseAssignments) {
                String item = converter.convert(assignment);
                if (visited.containsKey(item)) {
                    if (!reported.contains(item)) {
                        reported.add(item);
                        AcademicCourseAssignment a0 = visited.get(item);
                        log.severe("\t " + a0.getId() + " : " + item);
                    }
                    log.severe("\t " + assignment.getId() + " : " + item);
                } else {
                    visited.put(item, assignment);
                }
            }
            throw new IllegalArgumentException("Some Course Assignments have similar names. Check log for details");
        }
        for (AcademicCourseAssignment v : fromList) {
            if (!toList.containsMappedValue(v)) {
                AcademicCourseAssignment v2 = pu.copyObject(v);
                v2.setId(0);
                if (v2.getCoursePlan() != null) {
                    v2.setCoursePlan(coursePlanMap.get(v2.getCoursePlan().getId()));
                }
                pu.persist(v2);
                oldIdToNewObject.put(v.getId(), v2);
            } else {
                oldIdToNewObject.put(v.getId(), toList.getByValue(v));
            }
        }
        return oldIdToNewObject;
    }

    private Map<Integer, AcademicCourseIntent> _copyAcademicDataAcademicCourseIntent(int fromPeriodId, AppPeriod toPeriod, Map<Integer, AcademicCourseAssignment> courseAssignmentsMap, PersistenceUnit pu) {
        final AcademicPlugin p = VrApp.getBean(AcademicPlugin.class);
        Map<Integer, AcademicCourseIntent> oldIdToNewObject = new HashMap<>();
        Converter<String, AcademicCourseIntent> converter = new Converter<String, AcademicCourseIntent>() {
            @Override
            public String convert(AcademicCourseIntent value) {
                return value.getTeacher().getId() + ":" + value.getAssignment().getFullName();
            }
        };
        MapList<String, AcademicCourseIntent> fromList = new DefaultMapList<>(
                p.findCourseIntents(fromPeriodId),
                converter
        );

        MapList<String, AcademicCourseIntent> toList = new DefaultMapList<>(
                p.findCourseIntents(toPeriod.getId()),
                converter
        );
        for (AcademicCourseIntent v : fromList) {
            if (!toList.containsMappedValue(v)) {
                AcademicCourseIntent v2 = pu.copyObject(v);
                v2.setId(0);
                if (v2.getAssignment() != null) {
                    v2.setAssignment(courseAssignmentsMap.get(v2.getAssignment().getId()));
                }
                pu.persist(v2);
                oldIdToNewObject.put(v.getId(), v2);
            } else {
                oldIdToNewObject.put(v.getId(), toList.getByValue(v));
            }
        }
        return oldIdToNewObject;
    }

    public void copyAcademicData(int fromPeriodId, int toPeriodId) {
        final AcademicPlugin p = VrApp.getBean(AcademicPlugin.class);
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppPeriod toPeriod = VrApp.getBean(CorePlugin.class).findPeriod(toPeriodId);

        Map<Integer, AcademicCourseGroup> academicCourseGroupMap = _copyAcademicDataAcademicCourseGroup(fromPeriodId, toPeriod, pu);
        Map<Integer, AcademicCoursePlan> academicCoursePlanMap = _copyAcademicDataAcademicCoursePlan(fromPeriodId, toPeriod, academicCourseGroupMap, pu);
        academicCourseGroupMap.clear();
        for (AcademicTeacher old : p.findTeachers()) {
            p.updateTeacherPeriod(toPeriodId, old.getId(), fromPeriodId);
        }
        _copyAcademicDataAcademicTeacherSemestrialLoad(fromPeriodId, toPeriod, pu);
        Map<Integer, AcademicCourseAssignment> oldCoursePlanAssignmentsMap = _copyAcademicDataAcademicCourseAssignment(fromPeriodId, toPeriod, academicCoursePlanMap, pu);
        academicCoursePlanMap.clear();
        _copyAcademicDataAcademicCourseIntent(fromPeriodId, toPeriod, oldCoursePlanAssignmentsMap, pu);
        oldCoursePlanAssignmentsMap.clear();
    }

}
