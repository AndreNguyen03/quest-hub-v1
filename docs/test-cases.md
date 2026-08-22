# QuestHub — Test Cases

> 39 test files · 198 test cases

---

## Shared Infrastructure

**1. JwtServiceTest** — JWT token generation & parsing
```
1.1  accessTokenRoundTrip   — encode rồi decode access token phải khớp subject
                              — Hệ thống tạo access token cho user vừa đăng nhập, giải mã lại phải đúng userId

1.2  refreshTokenRoundTrip  — encode rồi decode refresh token phải khớp subject
                              — Hệ thống tạo refresh token, giải mã lại phải đúng userId để cấp token mới
```

**2. EmailTest** — Email value object validation
```
2.1  acceptsValidLowercaseEmails  — email lowercase hợp lệ được chấp nhận
                                    — User nhập "user@example.com" khi đăng ký → hợp lệ

2.2  rejectsUppercase             — email có chữ hoa bị từ chối
                                    — User nhập "User@Example.com" → bị báo lỗi validation

2.3  rejectsMalformed             — email sai format bị từ chối
                                    — User nhập "notanemail" hoặc "@example" → bị báo lỗi

2.4  rejectsBlankOrNull           — email rỗng / null bị từ chối
                                    — User để trống field email khi đăng ký → bị báo lỗi bắt buộc
```

**3. UsernameTest** — Username value object validation
```
3.1  acceptsLowercaseLettersDigitsUnderscore  — username hợp lệ được chấp nhận
                                               — User nhập "andre_nguyen03" khi đăng ký → hợp lệ

3.2  rejectsUppercase                         — username có chữ hoa bị từ chối
                                               — User nhập "AndreNguyen" → bị báo lỗi

3.3  rejectsSpacesAndSpecialChars             — username có ký tự đặc biệt bị từ chối
                                               — User nhập "andre nguyen!" → bị báo lỗi

3.4  rejectsBlankOrNull                       — username rỗng / null bị từ chối
                                               — User để trống field username → bị báo lỗi bắt buộc
```

**4. DisplayNameTest** — DisplayName value object validation
```
4.1  trimsWhitespace           — khoảng trắng đầu/cuối được trim
                                 — User nhập "  Andre Nguyen  " → lưu thành "Andre Nguyen"

4.2  acceptsVietnameseLetters  — tên tiếng Việt có dấu được chấp nhận
                                 — User nhập "Nguyễn Anh Đức" → hợp lệ

4.3  rejectsDigitsAndUnderscore — số và dấu gạch dưới bị từ chối
                                  — User nhập "Andre_123" làm display name → bị báo lỗi

4.4  rejectsBlank              — tên rỗng bị từ chối
                                 — User xóa trắng display name khi update profile → bị báo lỗi

4.5  enforcesMaxLength         — tên vượt 100 ký tự bị từ chối
                                 — User nhập tên dài hơn 100 ký tự → bị cắt ngắn / báo lỗi
```

---

## Identity BC

**5. LoginUseCaseTest** — Xác thực đăng nhập
```
5.1  login_whenCredentialsValid_shouldReturnUser       — email + mật khẩu đúng trả về User
                                                         — User nhập đúng email và mật khẩu → đăng nhập thành công

5.2  login_whenEmailNotFound_shouldThrowUnauthorized   — email không tồn tại → 401
                                                         — User nhập email chưa đăng ký → bị từ chối đăng nhập

5.3  login_whenPasswordWrong_shouldThrowUnauthorized   — mật khẩu sai → 401
                                                         — User nhập đúng email nhưng sai mật khẩu → bị từ chối

5.4  login_whenPasswordHashNull_shouldThrowUnauthorized — account chưa có password hash → 401
                                                          — User đăng ký qua OAuth nhưng cố login bằng password → bị từ chối
```

**6. RegisterUserUseCaseTest** — Đăng ký tài khoản
```
6.1  register_whenEmailFree_shouldCreateAndSaveUserWithEncodedPassword — đăng ký thành công, password được hash
                                                                         — User điền email và username mới → tài khoản tạo thành công, password hash lưu DB

6.2  register_whenEmailTaken_shouldThrowConflictWithEmailDetail        — email đã tồn tại → conflict
                                                                         — User nhập email đã có tài khoản → bị báo email đã dùng

6.3  register_whenUsernameTaken_shouldThrowConflictWithUsernameDetail  — username đã tồn tại → conflict
                                                                         — User chọn username đã có người dùng → bị báo username đã dùng

6.4  register_whenBothTaken_shouldGatherBothConflictsInOneThrow        — cả hai trùng → báo lỗi gộp
                                                                         — User nhập cả email lẫn username đã tồn tại → nhận 1 response chứa cả 2 lỗi
```

**7. UpdateProfileUseCaseTest** — Cập nhật profile
```
7.1  update_whenUserExists_shouldApplyAndSaveProfileChanges — avatar, bio, displayName, isPublic được lưu
                                                              — User vào Settings, đổi avatar và bio → lưu DB thành công

7.2  update_whenUserNotFound_shouldThrowNotFound            — user không tồn tại → 404
                                                              — Token hợp lệ nhưng user bị xóa khỏi DB → 404
```

**8. GetCurrentUserUseCaseTest** — Lấy thông tin user hiện tại
```
8.1  getById_whenUserExists_shouldReturnUser     — userId hợp lệ trả về User
                                                   — User gọi GET /me với token hợp lệ → nhận profile của mình

8.2  getById_whenUserMissing_shouldThrowNotFound — userId không tồn tại → 404
                                                   — Token hợp lệ nhưng user bị xóa → 404
```

**9. PromoteToCreatorUseCaseTest** — Nâng role USER lên CREATOR
```
9.1  promote_userRole_shouldSetCreatorRoleAndSave  — USER được nâng lên CREATOR và lưu
                                                     — User publish quest đầu tiên → role tự động chuyển sang CREATOR

9.2  promote_alreadyCreator_shouldNotSaveAgain     — đã là CREATOR → idempotent, không save lại
                                                     — Creator publish thêm quest nữa → role không thay đổi, không ghi DB lại

9.3  promote_userNotFound_shouldSkipGracefully     — user không tồn tại → bỏ qua, không lỗi
                                                     — Event promote gửi đến nhưng user đã bị xóa → hệ thống bỏ qua an toàn
```

---

## Quest BC — Domain

**10. CompletionRuleTest** — Validation của CompletionRule value object
```
10.1  defaultAllTasks_shouldHaveAllTasksType          — rule mặc định là ALL_TASKS
                                                        — Creator tạo Quest không set rule → hệ thống tự áp ALL_TASKS

10.2  quizScore_validThreshold_shouldKeepThreshold    — threshold hợp lệ (0–100) được giữ
                                                        — Creator set rule QUIZ_SCORE với ngưỡng 80% → lưu đúng 80

10.3  quizScore_missingThreshold_shouldThrow          — thiếu threshold → lỗi
                                                        — Creator set QUIZ_SCORE nhưng quên điền ngưỡng → bị báo lỗi

10.4  quizScore_thresholdOutOfRange_shouldThrow       — threshold ngoài 0–100 → lỗi
                                                        — Creator set ngưỡng 150% → bị báo lỗi

10.5  submission_emptyRequiredTaskTypes_shouldThrow   — SUBMISSION không có taskTypes → lỗi
                                                        — Creator set rule SUBMISSION nhưng không chỉ định loại task → bị báo lỗi

10.6  submission_validTypes_shouldKeep                — taskTypes hợp lệ được giữ
                                                        — Creator set SUBMISSION yêu cầu PRACTICE task → lưu đúng

10.7  allOf_lessThanTwoRules_shouldThrow              — ALL_OF cần ít nhất 2 sub-rules
                                                        — Creator set ALL_OF chỉ với 1 rule con → bị báo lỗi

10.8  allOf_withTwoRules_shouldKeepNestedRules        — ALL_OF với 2 rules hợp lệ được giữ
                                                        — Creator set "quiz 80% AND có submission" → lưu đúng 2 rule con
```

**11. CompletionEvaluatorTest** — Evaluate CompletionRule trên PersonalQuest
```
11.1  allTasks_completeAll_shouldSatisfy              — tất cả task xong → rule ALL_TASKS thỏa
                                                        — Learner tick hoàn thành task cuối cùng → quest tự động COMPLETED

11.2  quizScore_meetsThreshold_shouldSatisfy          — quiz đạt ngưỡng → rule QUIZ_SCORE thỏa
                                                        — Learner làm quiz đạt 85%, ngưỡng 80% → quest COMPLETED

11.3  quizScore_noQuizTasks_shouldNotSatisfy          — không có quiz task → rule không thỏa
                                                        — Quest có rule QUIZ_SCORE nhưng không có task QUIZ → không thể complete

11.4  submission_requiresAllTasksOfRequiredTypes      — các task type bắt buộc phải hoàn thành
                                                        — Learner nộp submission nhưng bỏ qua PRACTICE task bắt buộc → chưa complete

11.5  allOf_requiresAllSubRules                       — ALL_OF: tất cả sub-rules phải thỏa
                                                        — Learner làm xong quiz nhưng chưa nộp submission → chưa complete

11.6  anyOf_requiresOneSubRule                        — ANY_OF: chỉ cần 1 sub-rule thỏa
                                                        — Learner hoàn thành quiz hoặc nộp submission, một trong hai → quest complete
```

**12. QuizGraderTest** — Chấm điểm quiz
```
12.1  grade_8of10_withPassThreshold80_shouldPass            — 80% đúng >= ngưỡng 80% → PASS
                                                              — Learner trả lời đúng 8/10 câu, ngưỡng pass 80% → vượt qua

12.2  grade_7of10_withPassThreshold80_shouldFail            — 70% đúng < ngưỡng 80% → FAIL
                                                              — Learner trả lời đúng 7/10 câu, ngưỡng 80% → không vượt qua

12.3  grade_multipleChoiceList_shouldMatchWholeSet          — multi-choice phải khớp toàn bộ đáp án
                                                              — Learner chọn 2/3 đáp án đúng của câu multi-select → câu đó sai

12.4  grade_noQuestions_shouldReject                        — quiz không có câu hỏi → lỗi
                                                              — Creator tạo QUIZ task nhưng config.questions rỗng → hệ thống báo lỗi cấu hình

12.5  grade_missingPassThreshold_shouldDefaultToZeroAndPass — không có ngưỡng → default 0, luôn pass
                                                              — Creator quên set passThreshold → Learner submit bất kỳ đáp án nào cũng pass
```

**13. DistrictTest** — District domain entity
```
13.1  create_shouldStartAtZero                  — District mới có completionCount = 0
                                                  — Learner hoàn thành task ở domain mới lần đầu → District tạo mới với count = 0 trước khi tăng

13.2  incrementCompletion_shouldIncrease        — increment tăng count lên 1
                                                  — Mỗi lần Learner hoàn thành task → count trong District tương ứng tăng 1

13.3  decrementCompletion_shouldDecrease        — decrement giảm count xuống 1
                                                  — Learner undo task → count giảm 1

13.4  decrementCompletion_shouldNotGoBelowZero  — count không xuống dưới 0
                                                  — Learner undo nhiều lần hơn số lần hoàn thành (edge case) → count dừng ở 0
```

---

## Quest BC — Content

**14. CreateQuestUseCaseTest** — Tạo Quest mới
```
14.1  create_withNestedChaptersAndTasks_shouldAssignPositionsAndDefaultDraft — Quest tạo ở DRAFT, gán position cho chapter/task
                                                                               — Creator điền đầy đủ thông tin quest → Quest lưu DRAFT, các chapter/task có position đúng thứ tự

14.2  create_withQuizScoreRule_shouldKeepProvidedRule                        — CompletionRule được truyền vào được giữ nguyên
                                                                               — Creator chọn rule "QUIZ_SCORE 80%" khi tạo → rule được lưu chính xác

14.3  create_whenLearningPathMissing_shouldThrowNotFoundAndNotSave           — LearningPath không tồn tại → 404, không save
                                                                               — Creator gán quest vào learning path không tồn tại (ID sai) → bị báo 404
```

**15. PublishQuestUseCaseTest** — Publish Quest
```
15.1  publish_validTree_shouldSetPublicWriteOutboxAndPromoteCreator      — Quest PUBLIC, outbox event ghi, creator được promote
                                                                           — Creator bấm Publish quest đủ chapter/task → quest PUBLIC, role tự nâng CREATOR

15.2  publish_whenCreatorAlreadyHasPublicQuest_shouldNotPromoteAgain     — đã là CREATOR → không promote lại
                                                                           — Creator đã có quest public trước đó, publish thêm quest mới → role không đổi

15.3  publish_emptyQuest_shouldThrowDomainValidationAndNotSave           — Quest không có chapter/task → lỗi domain
                                                                           — Creator bấm Publish quest chưa có nội dung → bị báo lỗi

15.4  publish_alreadyPublic_shouldBeIdempotentWithoutSaveOrOutbox        — đã PUBLIC → idempotent, không save/publish
                                                                           — Creator bấm Publish lần 2 lên quest đã public → không làm gì thêm

15.5  publish_nonCreator_shouldThrowForbidden                            — không phải creator → 403
                                                                           — User cố publish quest của người khác → bị từ chối
```

**16. UnpublishQuestUseCaseTest** — Unpublish Quest về DRAFT
```
16.1  unpublish_publicQuest_shouldRevertToDraft         — Quest PUBLIC → DRAFT
                                                          — Creator bấm Unpublish → quest ẩn khỏi Marketplace, về DRAFT

16.2  unpublish_alreadyDraft_shouldBeIdempotentWithoutSave — đã DRAFT → idempotent, không save
                                                             — Creator bấm Unpublish quest đã ở DRAFT → không làm gì thêm

16.3  unpublish_nonCreator_shouldThrowForbidden         — không phải creator → 403
                                                          — User cố unpublish quest của người khác → bị từ chối
```

**17. SetCompletionRuleUseCaseTest** — Cấu hình CompletionRule
```
17.1  setRule_onDraftQuest_shouldPersistAndReturnRule  — Quest DRAFT → rule được lưu
                                                         — Creator chọn rule "QUIZ_SCORE" cho quest đang soạn → rule lưu thành công

17.2  setRule_nonCreator_shouldThrowForbidden          — không phải creator → 403
                                                         — User cố đổi rule quest người khác → bị từ chối

17.3  setRule_onPublicQuest_shouldThrowConflict        — Quest PUBLIC → không đổi rule được
                                                         — Creator cố đổi rule quest đã publish (structure locked) → bị báo 409
```

**18. CreateLearningPathUseCaseTest** — Tạo Learning Path
```
18.1  create_whenDomainExists_shouldCreatePrivatePathAndSave   — path private mặc định, được lưu
                                                                 — Creator tạo Learning Path thuộc domain "Programming" → path lưu private

18.2  create_whenDomainMissing_shouldThrowNotFoundAndNotSave   — domain không tồn tại → 404
                                                                 — Creator chọn domain không tồn tại → bị báo 404, path không tạo
```

**19. QuestForkedEventHandlerTest** — Xử lý event quest.forked
```
19.1  handle_questForked_shouldIncrementForkCount  — fork_count trên Quest tăng lên 1
                                                     — Learner fork quest → fork_count trên Quest gốc hiển thị tăng thêm 1

19.2  handle_otherEventType_shouldIgnore           — event type khác → bỏ qua
                                                     — Event quest.completed đến handler này → không làm gì
```

---

## Quest BC — Learning Progress

**20. ForkQuestUseCaseTest** — Fork Quest
```
20.1  fork_publicQuest_shouldCopyTreeWithSnapshotAndPublishEvent — copy toàn bộ chapter/task, snapshot CompletionRule, publish event
                                                                   — Learner bấm "Start Quest" trên quest public → PersonalQuest tạo với toàn bộ nội dung

20.2  fork_draftQuest_shouldThrowForbiddenWithoutSave            — Quest DRAFT không thể fork → 403
                                                                   — Learner cố fork quest chưa publish → bị từ chối

20.3  fork_alreadyForked_shouldThrowConflictWithoutSave          — đã fork rồi → 409
                                                                   — Learner bấm "Start Quest" lần 2 trên quest đã fork → bị báo đã có bản của mình

20.4  fork_unknownQuest_shouldThrowNotFound                      — Quest không tồn tại → 404
                                                                   — Learner truy cập quest bị xóa → 404

20.5  fork_snapshotIsIndependentOfOriginalChanges                — thay đổi Quest gốc sau khi fork không ảnh hưởng PersonalQuest
                                                                   — Creator sửa tên task sau khi Learner đã fork → PersonalQuest của Learner không thay đổi
```

**21. CompleteTaskUseCaseTest** — Hoàn thành task
```
21.1  complete_learnTask_shouldSetCompletedAndCreateCompletionAndPublishEvent  — LEARN task: tick xong, TaskCompletion tạo, event publish
                                                                                 — Learner đọc xong tài liệu, tick "Complete" trên LEARN task → task đánh dấu xong

21.2  complete_whenEvaluatorMarksQuestCompleted_shouldPublishQuestCompleted    — nếu quest complete → event quest.completed publish
                                                                                 — Learner hoàn thành task cuối cùng, rule thỏa → quest COMPLETED, World được cập nhật

21.3  complete_submissionWithoutEvidence_shouldReject                          — SUBMISSION không có evidence → lỗi domain
                                                                                 — Learner bấm Complete trên SUBMISSION task mà không điền link/nộp file → bị báo lỗi

21.4  complete_submissionWithUrl_shouldSucceed                                 — SUBMISSION có URL → thành công
                                                                                 — Learner điền link GitHub vào SUBMISSION task → task hoàn thành

21.5  complete_reflectionShorterThanMinLength_shouldReject                     — REFLECTION text ngắn hơn minLength → lỗi
                                                                                 — Learner viết reflection quá ngắn (Creator đặt minLength=50) → bị yêu cầu viết thêm

21.6  complete_reflectionLongEnough_shouldSucceed                              — REFLECTION text đủ dài → thành công
                                                                                 — Learner viết đủ ký tự yêu cầu → task hoàn thành

21.7  complete_quizTask_shouldReject                                           — QUIZ task không complete bằng endpoint này → lỗi
                                                                                 — Learner cố tick Complete trực tiếp trên QUIZ task → bị từ chối, phải dùng endpoint submit quiz

21.8  complete_alreadyCompleted_shouldThrowConflict                            — task đã hoàn thành rồi → 409
                                                                                 — Learner tick Complete lần 2 trên task đã xong → bị báo conflict

21.9  complete_unknownTask_shouldThrowNotFound                                 — task không tồn tại → 404
                                                                                 — Learner gửi request với task ID sai → 404

21.10 complete_unknownQuest_shouldThrowNotFound                                — quest không tồn tại → 404
                                                                                 — Learner gửi request với personal quest ID sai → 404
```

**22. UndoTaskUseCaseTest** — Undo hoàn thành task
```
22.1  undo_completedTask_shouldResetAndDeleteCompletionAndPublishEvent — task completed → reset, TaskCompletion xóa, event publish
                                                                         — Learner bấm Undo trên task đã xong → task về trạng thái chưa làm, tiến độ giảm

22.2  undo_incompleteTask_shouldBeNoOp                                 — task chưa hoàn thành → không làm gì
                                                                         — Learner bấm Undo trên task chưa làm → không có gì thay đổi

22.3  undo_unknownTask_shouldThrowNotFound                             — task không tồn tại → 404
                                                                         — Learner gửi request undo với task ID sai → 404

22.4  undo_unknownQuest_shouldThrowNotFound                            — quest không tồn tại → 404
                                                                         — Learner gửi request undo với personal quest ID sai → 404
```

**23. SubmitQuizUseCaseTest** — Nộp bài quiz
```
23.1  submit_pass_shouldCompleteTaskSaveAttemptAndPublishEvent              — pass → task complete, QuizAttempt lưu, event publish
                                                                              — Learner trả lời đúng đủ ngưỡng → task tự động hoàn thành, lịch sử attempt lưu

23.2  submit_pass_whenEvaluatorMarksQuestCompleted_shouldPublishQuestCompleted — quest complete sau quiz → event quest.completed publish
                                                                                 — Learner pass quiz là task cuối cùng → quest tự động COMPLETED

23.3  submit_fail_shouldSaveAttemptButNotCompleteTask                       — fail → QuizAttempt lưu nhưng task chưa complete
                                                                              — Learner trả lời chưa đủ ngưỡng → attempt lưu, task vẫn chưa xong, có thể thử lại

23.4  submit_nonQuizTask_shouldReject                                       — task không phải QUIZ → lỗi domain
                                                                              — Learner cố submit quiz cho LEARN task → bị từ chối

23.5  submit_unknownTask_shouldThrowNotFound                                — task không tồn tại → 404
                                                                              — Learner gửi request với task ID sai → 404

23.6  submit_unknownQuest_shouldThrowNotFound                               — quest không tồn tại → 404
                                                                              — Learner gửi request với personal quest ID sai → 404
```

**24. EvaluateCompletionUseCaseTest** — Evaluate CompletionRule sau mỗi task action
```
24.1  evaluate_allTasksCompleted_shouldMarkQuestCompleted    — tất cả task xong → quest COMPLETED
                                                              — Learner hoàn thành task cuối cùng → hệ thống tự chuyển quest sang COMPLETED

24.2  evaluate_partiallyCompleted_shouldStayActive           — còn task chưa xong → quest ACTIVE
                                                              — Learner hoàn thành một số task → quest vẫn ACTIVE, tiến độ hiển thị %

24.3  evaluate_undoAfterCompleted_shouldReopenToActive       — undo task sau khi quest complete → quest ACTIVE trở lại
                                                              — Learner undo một task sau khi quest đã COMPLETED → quest mở lại ACTIVE

24.4  evaluate_quizScore_bestAttemptMeetsThreshold           — quiz best attempt đạt ngưỡng → rule thỏa
                                                              — Learner fail lần 1, pass lần 2 → best attempt được dùng để evaluate

24.5  evaluate_alreadyCompleted_shouldNotRepublish           — quest đã COMPLETED → không publish event lại
                                                              — Learner complete một task trong quest đã hoàn thành → không có event quest.completed lặp lại
```

**25. EditPersonalQuestUseCaseTest** — Chỉnh sửa PersonalQuest
```
25.1  addChapter_shouldAppendChapterAndPersist              — thêm chapter mới, được lưu
                                                              — Learner muốn thêm phần học riêng → bấm Add Chapter, chapter mới xuất hiện

25.2  addTask_shouldAppendTaskNotCompletedAndRecalcProgress  — task mới chưa complete, progress tính lại
                                                              — Learner thêm task → progress % giảm xuống (mẫu số tăng)

25.3  addTask_unknownChapter_shouldThrow                    — chapter không tồn tại → lỗi
                                                              — Learner thêm task vào chapter bị xóa → bị báo lỗi

25.4  removeTask_shouldRemoveAndRecalcProgress              — xóa task, progress tính lại
                                                              — Learner xóa task không cần thiết → progress % tính lại

25.5  removeChapter_shouldRemoveAndRecalcProgress           — xóa chapter, progress tính lại
                                                              — Learner xóa toàn bộ chapter → progress tính lại

25.6  reorderChapters_shouldUpdatePositions                 — reorder chapter, position được cập nhật
                                                              — Learner kéo thả đổi thứ tự chapter → thứ tự lưu đúng

25.7  reorderTasks_shouldUpdateOrders                       — reorder task, order được cập nhật
                                                              — Learner đổi thứ tự task trong chapter → order lưu đúng

25.8  reorderChapters_mismatchedIds_shouldThrow             — ID không khớp → lỗi domain
                                                              — Client gửi danh sách ID không khớp với chapter thực có → bị báo lỗi

25.9  edit_otherUsersQuest_shouldThrowNotFound              — chỉnh sửa quest người khác → 404
                                                              — Learner A cố sửa PersonalQuest của Learner B → bị báo 404

25.10 edit_completedQuest_shouldThrowConflict               — quest đã complete → không chỉnh sửa được
                                                              — Learner cố thêm chapter vào quest đã COMPLETED → bị báo 409
```

**26. AbandonQuestUseCaseTest** — Abandon PersonalQuest
```
26.1  abandon_activeQuest_shouldSetStatusAbandoned         — ACTIVE → ABANDONED, được lưu
                                                             — Learner bấm "Give Up" trên quest đang học → quest chuyển ABANDONED, biến khỏi danh sách active

26.2  abandon_completedQuest_shouldThrowDomainValidationException — COMPLETED không thể abandon
                                                                    — Learner cố abandon quest đã hoàn thành → bị từ chối, quest hoàn thành không thể bỏ

26.3  abandon_questNotFound_shouldThrowNotFound            — quest không tồn tại → 404
                                                             — Learner gửi request abandon với ID sai → 404

26.4  abandon_alreadyAbandoned_shouldThrowDomainValidationException — đã ABANDONED không thể abandon lại
                                                                       — Learner cố abandon quest đã abandon → bị báo lỗi, terminal state
```

**27. GetQuestAnalyticsQueryTest** — Analytics quest cho Creator
```
27.1  get_asCreator_shouldReturnFullAnalytics              — trả đủ completionRate, taskDropOff với tỉ lệ đúng
                                                             — Creator vào trang analytics quest của mình → thấy completion rate và biểu đồ drop-off từng task

27.2  get_questNotFound_shouldThrowNotFound                — quest không tồn tại → 404
                                                             — Creator truy cập analytics quest bị xóa → 404

27.3  get_notCreator_shouldThrowNotFound                   — không phải creator → 404 (không leak thông tin)
                                                             — User cố xem analytics quest người khác → 404, không biết quest có tồn tại không

27.4  get_noForks_shouldReturnZeroCompletionRateAndEmptyDropOff — chưa có fork → completionRate=0, dropOff rỗng
                                                                   — Creator xem analytics quest mới publish chưa ai fork → hiển thị 0%
```

---

## Marketplace BC

**28. CreateReviewUseCaseTest** — Tạo review cho Quest
```
28.1  create_withValidFork_shouldPersistAndPublishEvent  — đã fork, chưa review → review lưu, event publish
                                                           — Learner đã hoàn thành quest, vào trang Quest, bấm đánh giá 5 sao → review lưu thành công

28.2  create_notForked_shouldThrowForbidden              — chưa fork → 403
                                                           — User xem quest nhưng chưa fork, cố đánh giá → bị yêu cầu fork trước

28.3  create_duplicateReview_shouldThrowConflict         — đã review rồi → 409
                                                           — Learner đã review rồi, cố review lần 2 → bị báo đã đánh giá rồi
```

**29. AddFavoriteUseCaseTest** — Lưu Quest vào Favorites
```
29.1  add_newFavorite_shouldSaveAndReturn   — favorite mới được lưu
                                              — Learner bấm icon ❤ trên quest → quest vào danh sách Favorites

29.2  add_duplicate_shouldThrowConflict     — đã favorite rồi → 409
                                              — Learner bấm ❤ lần 2 trên quest đã lưu → bị báo đã yêu thích rồi
```

---

## World BC

**30. BuildingUnlockServiceTest** — Mở khóa Building theo completion count
```
30.1  noCompletion_shouldNotUnlock                     — count=0 → không unlock gì
                                                         — Learner chưa hoàn thành task nào trong domain → District trống, không có building

30.2  countReachesFirstThreshold_shouldUnlockHouse     — đủ ngưỡng đầu → unlock house
                                                         — Learner hoàn thành đủ task để đạt ngưỡng đầu tiên → building "house" xuất hiện trong District

30.3  countAtFive_shouldUnlockHouseAndSchool           — count=5 → unlock cả house và school
                                                         — Learner đạt count=5 → cả house lẫn school xuất hiện trong World

30.4  alreadyUnlockedPosition_shouldBeIdempotent       — position đã unlock → không tạo lại
                                                         — Learner hoàn thành thêm task khi đã có đủ building → không tạo building trùng
```

**31. TaskCompletionEventHandlerTest** — Xử lý event task.completed/task.undone
```
31.1  taskCompleted_whenWorldExists_shouldCreateDistrictAndIncrement — task.completed → District tạo mới hoặc tăng count
                                                                       — Learner hoàn thành task domain "Programming" → District Programming tạo (nếu chưa có) và count tăng

31.2  taskCompleted_whenWorldNotFound_shouldSkip                     — world không tồn tại → bỏ qua
                                                                       — Event đến nhưng user chưa có World → hệ thống bỏ qua an toàn

31.3  taskUndone_shouldDecrement                                     — task.undone → District giảm count
                                                                       — Learner undo task → District count giảm, World cập nhật

31.4  duplicateEvent_shouldBeIdempotent                             — cùng event → không tăng count hai lần
                                                                       — Event task.completed gửi 2 lần (retry) → count chỉ tăng 1

31.5  eventWithoutDomain_shouldSkip                                  — event không có domainId → bỏ qua
                                                                       — Task hoàn thành nhưng quest không thuộc domain nào → World không cập nhật

31.6  unrelatedEvent_shouldSkip                                      — event type khác → bỏ qua
                                                                       — Event quest.forked đến handler này → không làm gì
```

**32. QuestCompletionEventHandlerTest** — Xử lý event quest.completed/quest.reopened
```
32.1  handle_questCompleted_shouldIncrementCountAndSave         — quest.completed → World.questCompletedCount tăng
                                                                   — Learner hoàn thành quest → World hiển thị số quest +1

32.2  handle_questCompleted_shouldEvaluateAchievements          — quest.completed → achievement criteria được evaluate
                                                                   — Learner hoàn thành quest → hệ thống kiểm tra xem đủ điều kiện achievement nào không

32.3  handle_questReopened_shouldDecrementCountAndSave          — quest.reopened → World.questCompletedCount giảm
                                                                   — Learner undo task làm quest reopen → World đồng bộ giảm count

32.4  handle_questReopened_shouldNotEvaluateAchievements        — quest.reopened → không evaluate achievement
                                                                   — Quest reopen → không kiểm tra achievement (chỉ kiểm tra khi complete)

32.5  handle_unknownEventType_shouldSkip                        — event type khác → bỏ qua
                                                                   — Event task.completed đến handler này → không làm gì

32.6  handle_worldNotFound_shouldSkipGracefully                 — world không tồn tại → bỏ qua, không lỗi
                                                                   — Event đến nhưng World chưa được tạo → bỏ qua an toàn, không crash

32.7  handle_questReopened_countAlreadyZero_shouldNotGoBelowZero — count không xuống dưới 0
                                                                    — Quest reopen khi count đã = 0 (edge case) → count giữ nguyên 0
```

**33. AchievementUnlockServiceTest** — Evaluate và unlock Achievement
```
33.1  evaluate_questCountCriteriaMet_shouldUnlockAndPublish                   — QUEST_COUNT đạt ngưỡng → unlock + event
                                                                                 — Learner hoàn thành quest thứ 5 → achievement "Five Quests" mở khóa, notification gửi

33.2  evaluate_questCountBelowThreshold_shouldNotUnlock                       — QUEST_COUNT chưa đạt → không unlock
                                                                                 — Learner hoàn thành quest thứ 3, ngưỡng là 5 → chưa đủ, không unlock

33.3  evaluate_taskCountCriteriaMet_shouldUnlock                              — TASK_COUNT (tổng district) đạt ngưỡng → unlock
                                                                                 — Learner hoàn thành task thứ 10 (tổng tất cả domain) → achievement "Ten Tasks" mở khóa

33.4  evaluate_domainTaskCountCriteriaMet_shouldUnlockWhenAnyDomainMeetsThreshold — DOMAIN_TASK_COUNT: 1 domain đạt là đủ
                                                                                     — Learner hoàn thành 5 task trong "Programming" → achievement "Domain Five" mở khóa dù domain khác chưa đủ

33.5  evaluate_alreadyUnlocked_shouldSkip                                     — achievement đã unlock → không unlock lại
                                                                                 — Achievement đã có trong UserAchievement → evaluate bỏ qua, không tạo duplicate

33.6  evaluate_noWorld_shouldReturnEarly                                      — user chưa có World → trả về sớm
                                                                                 — Event đến nhưng user chưa được tạo World → không evaluate achievement

33.7  evaluate_multipleAchievementsMet_shouldUnlockAll                        — nhiều criteria thỏa → unlock tất cả
                                                                                 — Learner hoàn thành task thứ 10 đồng thời thỏa cả TASK_COUNT và QUEST_COUNT → cả hai achievement mở khóa
```

**34. UserRegisteredEventHandlerTest** — Tạo World khi User đăng ký
```
34.1  handle_userRegistered_shouldCreateWorldWithCorrectUserId  — user.registered → World tạo đúng userId + username
                                                                   — User đăng ký xong → World cá nhân tự động tạo, sẵn sàng hiển thị

34.2  handle_userRegistered_worldAlreadyExists_shouldSkip       — world đã tồn tại → idempotent, không tạo lại
                                                                   — Event user.registered gửi 2 lần (retry) → World không bị tạo trùng

34.3  handle_otherEventType_shouldIgnore                        — event type khác → bỏ qua
                                                                   — Event quest.completed đến handler này → không tạo World
```

---

## Integration Tests

**35. RegisterFlowIntegrationTest** — Luồng đăng ký và xác thực end-to-end
```
35.1  register_valid_shouldReturn201WithTokensAndPersistUser       — đăng ký thành công → 201, tokens, user lưu DB
                                                                      — User mới điền form đăng ký → nhận token, tài khoản lưu DB thật

35.2  login_validCredentials_shouldReturnTokens                    — đăng nhập đúng → access + refresh token
                                                                      — User nhập đúng email/password → nhận cặp token để dùng

35.3  login_wrongPassword_shouldReturn401InvalidCredentials        — sai mật khẩu → 401
                                                                      — User nhập sai password → 401, không nhận token

35.4  refresh_withValidRefreshToken_shouldRotateAndIssueNewPair    — refresh token → xoay vòng, cặp token mới
                                                                      — Access token hết hạn, dùng refresh token → nhận cặp token mới, refresh token cũ không dùng được nữa

35.5  updateProfile_withAccessToken_shouldUpdateAndPersist         — update profile có token → lưu DB
                                                                      — User đổi bio và avatar qua API → lưu DB, GET /me trả về thông tin mới

35.6  protectedEndpoint_withoutToken_shouldReturn401               — không có token → 401
                                                                      — Client gọi API cần xác thực nhưng không đính kèm token → 401

35.7  getMe_withAccessToken_shouldReturnCurrentUserProfile         — GET /me có token → trả profile
                                                                      — User gọi GET /me với token hợp lệ → nhận đúng profile của mình

35.8  getMe_withoutToken_shouldReturn401                           — GET /me không có token → 401
                                                                      — Client gọi GET /me không có Authorization header → 401

35.9  register_duplicateEmailAndUsername_shouldReturn409WithBothDetails — trùng email + username → 409 gộp lỗi
                                                                           — User dùng cả email lẫn username đã tồn tại → 1 response chứa cả 2 lỗi cùng lúc

35.10 register_invalidPayload_shouldReturn400ValidationError       — payload sai → 400
                                                                      — User submit form thiếu field bắt buộc → 400 với chi tiết field lỗi
```

**36. LearningPathFlowIntegrationTest** — Luồng Learning Path end-to-end
```
36.1  createPath_withValidDomain_shouldReturn201AndPersist     — tạo path → 201, lưu DB
                                                                 — Creator tạo Learning Path thuộc domain hợp lệ → 201, path có trong DB

36.2  createPath_withMissingDomain_shouldReturn404             — domain không tồn tại → 404
                                                                 — Creator chọn domain ID không có → 404, path không tạo

36.3  getPath_ownPrivatePath_shouldReturn200                   — owner xem path private → 200
                                                                 — Creator GET path của mình dù đang private → 200

36.4  getPath_otherUsersPrivatePath_shouldReturn403            — user khác xem path private → 403
                                                                 — User cố GET path private của người khác → 403

36.5  updatePath_byOwner_shouldUpdateAndPersist                — owner update path → lưu DB
                                                                 — Creator đổi title và difficulty của path → lưu DB thành công
```

**37. QuestFlowIntegrationTest** — Luồng Quest + PersonalQuest end-to-end
```
37.1  createQuest_withNestedTree_shouldReturn201AndPersistAllLevels   — tạo quest có chapter/task → 201, lưu tất cả levels
                                                                         — Creator tạo quest đầy đủ chapter/task → 201, tất cả entities lưu DB

37.2  createQuest_emptyChapters_shouldReturn400ValidationError        — quest không có chapter → 400
                                                                         — Creator tạo quest không có chapter → 400

37.3  getQuest_ownDraft_shouldReturn200                               — creator xem draft → 200
                                                                         — Creator GET quest đang DRAFT của mình → 200

37.4  getQuest_otherUsersDraft_shouldReturn403                        — user khác xem draft → 403
                                                                         — User GET quest DRAFT của người khác → 403

37.5  setCompletionRule_onDraft_shouldPersistAndReturnRule            — set rule trên draft → lưu
                                                                         — Creator set CompletionRule cho quest đang DRAFT → lưu thành công

37.6  setCompletionRule_nonCreator_shouldReturn403                    — không phải creator → 403
                                                                         — User cố set rule quest người khác → 403

37.7  setCompletionRule_publicQuest_shouldReturn409                   — quest đã public → 409
                                                                         — Creator set rule trên quest đã PUBLIC → 409 (structure locked)

37.8  forkQuest_publicQuest_shouldCopyTreeAndPersistPersonalQuest     — fork public → PersonalQuest đầy đủ
                                                                         — Learner fork quest public → PersonalQuest + chapters + tasks lưu DB đầy đủ

37.9  forkQuest_draftQuest_shouldReturn403                            — fork draft → 403
                                                                         — Learner cố fork quest chưa publish → 403

37.10 forkQuest_secondFork_shouldReturn409                            — fork lần 2 → 409
                                                                         — Learner đã có PersonalQuest, fork lại → 409

37.11 getPersonalQuest_owner_shouldReturn200_otherUserShouldReturn404 — owner thấy, user khác không thấy
                                                                         — Learner A GET PersonalQuest của mình → 200; Learner B GET PersonalQuest của A → 404

37.12 completeAndUndoTask_shouldPersistCompletionAndRevert            — complete rồi undo → DB phản ánh đúng
                                                                         — Learner tick complete rồi undo task → DB phản ánh đúng cả hai thay đổi

37.13 completeTask_submissionWithoutEvidence_shouldReturn400          — SUBMISSION không evidence → 400
                                                                         — Learner tick Complete trên SUBMISSION task không có link → 400

37.14 completeTask_otherUser_shouldReturn404                          — user khác complete task → 404
                                                                         — Learner A cố complete task trong PersonalQuest của Learner B → 404

37.15 submitQuiz_pass_shouldCompleteTaskAndPersistRecords             — quiz pass → task complete, QuizAttempt lưu
                                                                         — Learner submit đáp án đúng đủ ngưỡng → task complete, attempt lưu DB

37.16 submitQuiz_failThenPass_shouldShowBothInHistory                 — fail rồi pass → lịch sử đủ 2 attempt
                                                                         — Learner fail lần 1, pass lần 2 → GET history trả về cả 2 attempt

37.17 submitQuiz_otherUser_shouldReturn404                            — user khác submit quiz → 404
                                                                         — Learner A cố submit quiz vào PersonalQuest của Learner B → 404

37.18 editPersonalQuest_addTask_shouldNotAffectOriginalOrOtherFork    — thêm task không ảnh hưởng quest gốc / fork khác
                                                                         — Learner A thêm task vào PersonalQuest → Quest gốc và PersonalQuest của Learner B không đổi

37.19 editPersonalQuest_completedQuest_shouldReturn409                — quest complete → không edit được
                                                                         — Learner cố thêm chapter vào quest đã COMPLETED → 409

37.20 editPersonalQuest_otherUser_shouldReturn404                     — user khác edit → 404
                                                                         — Learner A cố sửa PersonalQuest của Learner B → 404

37.21 editPersonalQuest_reorderChapters_shouldPersistPositions        — reorder → position lưu đúng
                                                                         — Learner kéo chapter lên đầu → GET quest trả về đúng thứ tự mới

37.22 completeAllTasks_shouldCompleteQuestAndPublishEvent             — hoàn thành tất cả → quest COMPLETED + event
                                                                         — Learner tick xong task cuối cùng → quest COMPLETED, World được cập nhật qua event

37.23 undoAfterCompletion_shouldReopenQuestToActive                   — undo sau khi complete → quest ACTIVE trở lại
                                                                         — Learner undo task sau khi quest đã COMPLETED → quest status = ACTIVE, completedAt = null
```

**38. WorldFlowIntegrationTest** — Luồng World end-to-end
```
38.1  world_me_shouldReflectTaskCompletionsAndUndo      — complete/undo task → World District count đúng
                                                          — Learner hoàn thành task, undo, rồi complete lại → District count luôn đúng theo thứ tự action

38.2  world_me_otherUser_shouldReturnOwnEmptyWorld      — user khác không ảnh hưởng world của mình
                                                          — Learner B hoàn thành task → World của Learner A không thay đổi

38.3  districtDetail_shouldListBuildingsAndQuests       — district detail hiển thị building và quest đúng
                                                          — Learner mở District → thấy danh sách building và quest đang active / đã complete đúng

38.4  districtDetail_manualCount_shouldLazilyUnlockBuildings — count tăng → building unlock theo ngưỡng
                                                               — Learner đạt đủ số task → building mới hiện ra trong District khi load lại

38.5  districtDetail_otherUser_shouldReturn404          — xem district của user không public → 404
                                                          — Learner A xem District của Learner B (profile private) → 404
```

**39. ModulithTest** — Kiểm tra kiến trúc module
```
39.1  verifyModularity          — các module không có circular dependency, ranh giới được tôn trọng
                                  — CI chạy test này để đảm bảo không có module nào import trái phép vào module khác

39.2  writeDocumentation        — sinh documentation diagram tự động
                                  — CI tự sinh diagram kiến trúc từ code thực tế

39.3  verifyModulesIndividually — từng module compile và load độc lập
                                  — Mỗi bounded context được verify tách biệt, phát hiện rò rỉ dependency sớm
```

---

> **Tổng:** 39 files · 198 test cases
