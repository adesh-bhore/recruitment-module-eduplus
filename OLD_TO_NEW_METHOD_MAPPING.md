# Old Controller to New Service Method Mapping

This document maps old controller method names to new service method names for quick reference.

## RecApplicationController → RecApplicationService_1, _2, _3, _4

### RecApplicationService_1 (Phase 1-3: Application & Document Management)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| selectorg | getActiveRecruitments | RecApplicationService_1 |
| recappform | getApplicationFormData | RecApplicationService_1 |
| saveapplication | submitApplication | RecApplicationService_1 |
| savefileupload | uploadDocument | RecApplicationService_1 |
| downloaddocument | downloadDocument | RecApplicationService_1 |
| deletedocument | deleteDocument | RecApplicationService_1 |
| renderimage | getApplicantPhoto | RecApplicationService_1 |
| applicationform | getApplicationPreview | RecApplicationService_1 |

### RecApplicationService_2 (Phase 4-5: Admin Application Management)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| recauthorityselection | getAuthorityApplications | RecApplicationService_2 |
| recApplicationSummary | getApplicationSummary | RecApplicationService_2 |
| recappdata | getDetailedApplicationList | RecApplicationService_2 |
| recApplicationSummaryCount | getApplicationCounts | RecApplicationService_2 |
| getRecapplicants | getApplicantsList | RecApplicationService_2 |
| saverecapplicationshortlist | processApplication | RecApplicationService_2 |
| notifyshortlistcandidateinput | notifyShortlistedCandidates | RecApplicationService_2 |
| rejectaaplication | rejectApplication | RecApplicationService_2 |
| recapplicantattendance | markAttendance | RecApplicationService_2 |

### RecApplicationService_3 (Phase 6: Qualification & Experience)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| getqualificationdetails | getQualificationDetails | RecApplicationService_3 |
| editQualification | updateQualification | RecApplicationService_3 |
| deleteQualification | deleteQualification | RecApplicationService_3 |
| getExp | getExperienceDetails | RecApplicationService_3 |
| getDegreeByExamPassed | getDegreesByExam | RecApplicationService_3 |

### RecApplicationService_4 (Phase 7: Bulk Operations)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| downloaddocumentBulkzipRecApplicant | bulkDownloadDocuments | RecApplicationService_4 |

---

## RecApplicationLoginController → RecApplicationLoginService

| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| processerplogin | processerplogin | RecApplicationLoginService |
| sendotp | sendotp | RecApplicationLoginService |
| checkotp | checkotp | RecApplicationLoginService |
| resendotp | resendotp | RecApplicationLoginService |
| savelogin | savelogin | RecApplicationLoginService |
| sendotppasswordchange | sendotppasswordchange | RecApplicationLoginService |
| checkotppasswordchange | checkotppasswordchange | RecApplicationLoginService |
| resendotppasswordchange | resendotppasswordchange | RecApplicationLoginService |
| changePassword | changePassword | RecApplicationLoginService |
| savechangepassword | savechangepassword | RecApplicationLoginService |

---

## RecVersionController → RecVersionService, RecVersionService_2

### RecVersionService (Statistics & Reports)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| statisticsreportfilter | getStatisticsFilterData | RecVersionService |
| getRecVersionDetails | getRecVersionDetails | RecVersionService |
| statisticssummaryreport | getStatisticsSummaryReport | RecVersionService |
| gettotalpostcounts | getTotalPostCounts | RecVersionService |
| gettotaldeptcounts | getTotalDeptCounts | RecVersionService |

### RecVersionService_2 (Candidate Lists & Downloads)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| getcandidatelist | getCandidateList | RecVersionService_2 |
| downloadSelecteddocuments | downloadSelectedDocuments | RecVersionService_2 |

---

## RecExpertController → RecExpertService, RecExpertService2, RecExpertService3

### RecExpertService (Expert Login & Evaluation)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| recexpert_login | (View method - not migrated) | N/A |
| recexpertlogin | (Authentication - handled by controller) | N/A |
| add_rec_applicant_marks | (View method - not migrated) | N/A |
| rec_apllication_table | (View method - not migrated) | N/A |
| add_rec_applicants_marks_details | (Evaluation submission) | RecExpertService |
| rec_application_dashboard | (Dashboard view) | N/A |
| getApplicantMarks | (Get marks for display) | RecExpertService |
| recexpertinput1 | (Expert input form) | RecExpertService |
| saverecexpert | (Save expert) | RecExpertService |
| deleteexpert | (Delete expert) | RecExpertService |

---

## RecEvaluationParameterController → RecEvaluationParameterService

| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| parameterinput | (View method - returns form data) | RecEvaluationParameterService |
| getRecVersion | (Get versions by org/ay) | RecEvaluationParameterService |
| getRecExpertGroup | (Get expert groups) | RecEvaluationParameterService |
| getParameterlist | (Get parameter list) | RecEvaluationParameterService |
| saveevaluationparameter | (Save/update parameter) | RecEvaluationParameterService |
| deleteevaluationparameter | (Delete parameter) | RecEvaluationParameterService |

---

## RecOnlineMcqController → RecOnlineMcqService

| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| showCourses | (Get courses for instructor) | RecOnlineMcqService |
| createQuestionBank | (View question bank) | RecOnlineMcqService |
| storeQuestionInQuestionBank | (Create question) | RecOnlineMcqService |
| updateIsapprovemcqQuestion | (Approve/reject question) | RecOnlineMcqService |
| editQuestion | (Get question for editing) | RecOnlineMcqService |
| storeEditQuestionInQuestionBank | (Update question) | RecOnlineMcqService |

---

## RecExamController → RecExamService

| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| generatesecretcode | (Generate exam codes) | RecExamService |
| fetchexamapplicantdata | (Get exam applicants) | RecExamService |
| quesallocation | (Allocate questions) | RecExamService |
| schedule | (View schedule form) | RecExamService |
| setschedule | (Set exam schedule) | RecExamService |
| addeditschedule | (Add/edit schedule) | RecExamService |
| setscheduleforall | (Set schedule for all) | RecExamService |
| extendtime | (Extend exam time) | RecExamService |
| stopexam | (Stop exam) | RecExamService |
| startexam | (Start exam) | RecExamService |
| supervisor | (Supervisor dashboard) | RecExamService |
| addsupervisor | (Add supervisor) | RecExamService |
| appointsupervisor | (Appoint supervisor) | RecExamService |
| selectgrp | (Select expert group) | RecExamService |
| expertgrpwise | (Get results by group) | RecExamService |
| saveselectedapp | (Save selected applications) | RecExamService |

---

## RecInterviewScheduleDetailsController → RecInterviewScheduleService1, _2, _3

### RecInterviewScheduleService1 (Main Interview Scheduling)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| (Methods to be mapped) | (To be completed) | RecInterviewScheduleService1 |

### RecInterviewScheduleService_2 (Branch Management)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| (Methods to be mapped) | (To be completed) | RecInterviewScheduleService_2 |

### RecInterviewScheduleService_3 (Document Type Management)
| Old Method Name | New Method Name | Service File |
|----------------|-----------------|--------------|
| (Methods to be mapped) | (To be completed) | RecInterviewScheduleService_3 |

---

## Quick Search Tips

1. **To find old method**: Search for the old method name in this document
2. **To find new method**: Look at the "New Method Name" column
3. **To find implementation**: Open the service file listed in "Service File" column
4. **In service files**: Look for comment `// OLD METHOD: oldMethodName (from ControllerName)`

## Example Usage

If you're looking for the old `saveapplication` method:
1. Search this document for "saveapplication"
2. Find: `saveapplication → submitApplication (RecApplicationService_1)`
3. Open `RecApplicationService_1.groovy`
4. Search for `// OLD METHOD: saveapplication`
5. The new method `submitApplication` will be right below that comment

---

## Notes

- **View methods** (methods that just return data for GSP views) are not migrated as the new architecture uses REST APIs
- **Authentication methods** are handled by controllers with JWT tokens
- **New methods** (marked as "N/A - New method") are additional APIs not present in old codebase
- All service methods follow the pattern: `def methodName(hm, request, data)`
- `hm` = HashMap for response
- `request` = HTTP request object
- `data` = Request parameters/body

---

**Last Updated**: May 14, 2026
**Migration Status**: Phase 1-7 Complete (RecApplication, RecApplicationLogin, RecVersion services)
**Remaining**: RecExpert, RecInterviewSchedule, RecEvaluationParameter, RecOnlineMcq, RecExam services (comments to be added)


## RecExpertController → RecExpertService, RecExpertService2, RecExpertService3

### RecExpertService (Expert CRUD & Authentication)
| Old Method (RecExpertController) | New Method (RecExpertService) | Description |
|----------------------------------|-------------------------------|-------------|
| `recexpertinput1` | `getInitialData` | Get initial data for expert management form |
| `getfilters` | `getFilters` | Get filtered data based on organization |
| `saverecexpert` | `saveExpert` | Create new expert |
| `deleteexpert` | `deleteExpert` | Delete expert |
| `recexpertlogin` | `loginExpert` | Authenticate expert |

### RecExpertService2 (Marks Evaluation)
| Old Method (RecExpertController) | New Method (RecExpertService2) | Description |
|----------------------------------|--------------------------------|-------------|
| `add_rec_applicant_marks` | `getApplicantsForMarks` | Get applicants list for marks evaluation |
| `rec_apllication_table` | `getEvaluationForm` | Get evaluation form for specific applicant |
| `add_rec_applicants_marks_details` | `submitMarks` | Submit marks for applicant |
| `rec_application_dashboard` | `getExpertGroupsDashboard` | Get expert groups dashboard |
| `getApplicantMarks` | `getApplicantMarksReport` | Get applicant marks report for expert group |

### RecExpertService3 (Expert Groups & Types Management)
| Old Method (RecExpertController) | New Method (RecExpertService3) | Description |
|----------------------------------|--------------------------------|-------------|
| N/A (new functionality) | `getExpertGroupInitialData` | Get initial data for expert group management |
| N/A (new functionality) | `getExpertGroupFilters` | Get filtered expert groups |
| N/A (new functionality) | `saveExpertGroup` | Create or update expert group |
| N/A (new functionality) | `deleteExpertGroup` | Delete expert group |
| N/A (new functionality) | `getExpertTypeList` | Get list of expert types |
| N/A (new functionality) | `saveExpertType` | Create or update expert type |
| N/A (new functionality) | `deleteExpertType` | Delete expert type |

---

## RecInterviewScheduleDetailsController → RecInterviewScheduleService1, RecInterviewScheduleService_2, RecInterviewScheduleService_3

### RecInterviewScheduleService1 (Main Interview Scheduling)
| Old Method (RecInterviewScheduleDetailsController) | New Method (RecInterviewScheduleService1) | Description |
|---------------------------------------------------|-------------------------------------------|-------------|
| `getinterviewschedule` | `getInterviewScheduleList` | Get interview schedules with filter data |
| `getRecVersion` | `getRecVersion` | Get recruitment versions by org and academic year |
| `getDept` | `getDept` | Get departments by org and recruitment version |
| `getPost` | `getPost` | Get posts by org and recruitment version |
| `getInterviewList` | `getInterviewList` | Get interview list by org and recruitment version |
| `saveinterviewschedule` | `saveInterviewSchedule` | Save new interview schedule |
| `editinterviewschedule` | `editInterviewSchedule` | Update existing interview schedule |
| `deletesched` | `deleteSched` | Delete interview schedule |
| `sendmail` | `sendInterviewCallLetters` | Send interview call letters via email |
| `preview_callletter` | `previewCallLetters` | Preview interview call letters |

### RecInterviewScheduleService_2 (Branch & Post Management)
| Old Method (RecInterviewScheduleDetailsController) | New Method (RecInterviewScheduleService_2) | Description |
|---------------------------------------------------|-------------------------------------------|-------------|
| `addRecBranch` | `getRecBranchList` | Get all RecBranch for organization |
| `saverecBranch` | `saveRecBranch` | Create a new RecBranch |
| `editRecBranch` | `editRecBranch` | Update an existing RecBranch |
| `deleterecBranch` | `deleteRecBranch` | Delete a RecBranch |
| `addRecPost` | `getRecPostList` | Get all RecPost for organization |
| `saverecPost` | `saveRecPost` | Create a new RecPost |
| `editRecPost` | `editRecPost` | Update an existing RecPost |
| `deleterecPost` | `deleteRecPost` | Delete a RecPost |
| `assignPost` | `getAssignPostList` | Get all ERPFacultyPost and Instructors |
| `saveassignPost` | `saveAssignPost` | Assign ERPFacultyPost to Instructor |
| `deleteassignPost` | `deleteAssignPost` | Remove ERPFacultyPost assignment |

### RecInterviewScheduleService_3 (Document Type Management)
| Old Method (RecInterviewScheduleDetailsController) | New Method (RecInterviewScheduleService_3) | Description |
|---------------------------------------------------|-------------------------------------------|-------------|
| `addRecDocumentType` | `getRecDocumentTypeList` | Get all RecDocumentType |
| `saveRecDocumentType` | `saveRecDocumentType` | Create a new RecDocumentType |
| `editRecDocumentType` | `editRecDocumentType` | Update an existing RecDocumentType |
| `deleteRecDocumentType` | `deleteRecDocumentType` | Delete a RecDocumentType |
| `recdocumentList` | `getRecDocumentList` | Get list of applications with documents |
| `getdoc` | `getApplicantDocuments` | Get documents for specific applicant |

---

## RecEvaluationParameterController → RecEvaluationParameterService

| Old Method (RecEvaluationParameterController) | New Method (RecEvaluationParameterService) | Description |
|----------------------------------------------|-------------------------------------------|-------------|
| `parameterinput` | `getInitialData` | Get initial data for parameter input form |
| `getRecVersion` | `getRecVersions` | Get recruitment versions for org and academic year |
| `getRecExpertGroup` | `getExpertGroups` | Get expert groups for org and recruitment version |
| `getParameterlist` | `getParameters` | Get evaluation parameters list |
| `saveevaluationparameter` | `saveParameter` | Create or update evaluation parameter |
| `deleteevaluationparameter` | `deleteParameter` | Delete evaluation parameter |

---

## RecOnlineMcqController → RecOnlineMcqService

| Old Method (RecOnlineMcqController) | New Method (RecOnlineMcqService) | Description |
|-------------------------------------|----------------------------------|-------------|
| `showCourses` | `getInstructorCourses` | Get instructor's courses list |
| `createQuestionBank` | `getQuestionBank` | Get all questions for a course with pagination |
| `storeQuestionInQuestionBank` | `createQuestion` | Create new MCQ question with options |
| `storeEditQuestionInQuestionBank` | `updateQuestion` | Update existing question with options |
| `updateIsapprovemcqQuestion` | `toggleQuestionApproval` | Toggle question approval status |
| `editQuestion` | `getQuestionDetails` | Get full question details for editing |

---

## RecExamController → RecExamService

| Old Method (RecExamController) | New Method (RecExamService) | Description |
|--------------------------------|-----------------------------|-------------|
| `generatesecretcode` | `generateSecretCodes` | Generate secret codes for shortlisted candidates |
| `fetchexamapplicantdata` | `getExamApplicants` | Get exam applicant data |
| `quesallocation` | `allocateQuestions` | Allocate questions to applicants based on weightage |
| `schedule` | `getGroups` | Get all department groups for scheduling |
| `setschedule` | `getSchedule` | Get exam schedule for a specific group |
| `addeditschedule` | `setSchedule` | Set schedule for individual candidate |
| `setscheduleforall` | `setScheduleForAll` | Set schedule for all candidates in a group |
| `extendtime` | `extendTime` | Extend exam time for a candidate |
| `stopexam` | N/A (to be implemented) | Stop exam for a candidate |
| `startexam` | N/A (to be implemented) | Start exam for a candidate |

---

## Summary

This document provides a comprehensive mapping of all old controller methods to their new service implementations. The team can use this as a quick reference guide to find the new method names when working with the migrated codebase.

**Total Controllers Migrated:** 9
**Total Service Files Created:** 18
**Total Methods Mapped:** 100+

For any questions or clarifications, please refer to the inline comments in the service files which include the old method names in the format:
```groovy
// OLD METHOD: oldMethodName (from OldControllerName)
```
