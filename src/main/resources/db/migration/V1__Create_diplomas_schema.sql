CREATE TABLE "user"
(
    id       UUID         NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL,
    active   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_user PRIMARY KEY (id)
);
 
CREATE TABLE cohort
(
    id         UUID    NOT NULL,
    entry_year INTEGER NOT NULL,
    CONSTRAINT pk_cohort PRIMARY KEY (id)
);
 
CREATE TABLE track
(
    id   UUID         NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_track PRIMARY KEY (id)
);
 
CREATE TABLE "group"
(
    id        UUID        NOT NULL,
    code      VARCHAR(50) NOT NULL,
    track_id  UUID,
    cohort_id UUID        NOT NULL,
    CONSTRAINT pk_group PRIMARY KEY (id)
);
 
CREATE TABLE teacher
(
    id         UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    last_name  VARCHAR(200) NOT NULL,
    first_name VARCHAR(200) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_teacher PRIMARY KEY (id)
);
 
CREATE TABLE student
(
    id             UUID         NOT NULL,
    user_id        UUID         NOT NULL,
    cohort_id      UUID         NOT NULL,
    last_name      VARCHAR(200) NOT NULL,
    first_name     VARCHAR(200) NOT NULL,
    student_number VARCHAR(50)  NOT NULL,
    work_study     BOOLEAN      NOT NULL DEFAULT FALSE,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_student PRIMARY KEY (id)
);
 
CREATE TABLE student_group_history
(
    id         UUID NOT NULL,
    student_id UUID NOT NULL,
    group_id   UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date   DATE,
    CONSTRAINT pk_student_group_history PRIMARY KEY (id)
);
 
CREATE TABLE course
(
    id       UUID         NOT NULL,
    code     VARCHAR(50)  NOT NULL,
    title    VARCHAR(255) NOT NULL,
    credits  INTEGER      NOT NULL,
    level    VARCHAR(10)  NOT NULL,
    semester VARCHAR(10)  NOT NULL,
    CONSTRAINT pk_course PRIMARY KEY (id)
);
 
CREATE TABLE track_course
(
    track_id  UUID NOT NULL,
    course_id UUID NOT NULL,
    CONSTRAINT pk_track_course PRIMARY KEY (track_id, course_id)
);
 
CREATE TABLE course_assignment
(
    id            UUID        NOT NULL,
    course_id     UUID        NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    CONSTRAINT pk_course_assignment PRIMARY KEY (id)
);
 
CREATE TABLE course_assignment_teaching
(
    id            UUID NOT NULL,
    assignment_id UUID NOT NULL,
    teacher_id    UUID NOT NULL,
    group_id      UUID NOT NULL,
    CONSTRAINT pk_course_assignment_teaching PRIMARY KEY (id)
);
 
CREATE TABLE exam
(
    id            UUID                        NOT NULL,
    assignment_id UUID                        NOT NULL,
    label         VARCHAR(255)                NOT NULL,
    exam_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    coefficient   DOUBLE PRECISION            NOT NULL,
    type          VARCHAR(20)                 NOT NULL,
    CONSTRAINT pk_exam PRIMARY KEY (id)
);
 
CREATE TABLE grade
(
    id          UUID             NOT NULL,
    student_id  UUID             NOT NULL,
    exam_id     UUID             NOT NULL,
    value       DOUBLE PRECISION NOT NULL,
    graded_date DATE             NOT NULL,
    CONSTRAINT pk_grade PRIMARY KEY (id)
);
 
CREATE TABLE grade_history
(
    id          UUID                        NOT NULL,
    grade_id    UUID                        NOT NULL,
    old_value   DOUBLE PRECISION            NOT NULL,
    new_value   DOUBLE PRECISION            NOT NULL,
    reason      VARCHAR(500)                NOT NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_grade_history PRIMARY KEY (id)
);
 
CREATE TABLE transcript_delivery
(
    id            UUID                        NOT NULL,
    student_id    UUID                        NOT NULL,
    academic_year VARCHAR(20)                 NOT NULL,
    sent_at       TIMESTAMP WITHOUT TIME ZONE,
    s3_url        VARCHAR(1000),
    status        VARCHAR(20)                 NOT NULL,
    CONSTRAINT pk_transcript_delivery PRIMARY KEY (id)
);
 
ALTER TABLE "user"
    ADD CONSTRAINT uc_user_email UNIQUE (email);
 
ALTER TABLE student
    ADD CONSTRAINT uc_student_number UNIQUE (student_number);
 
ALTER TABLE track
    ADD CONSTRAINT uc_track_name UNIQUE (name);
 
ALTER TABLE course
    ADD CONSTRAINT uc_course_code UNIQUE (code);
 
ALTER TABLE "group"
    ADD CONSTRAINT FK_GROUP_ON_TRACK FOREIGN KEY (track_id) REFERENCES track (id);
 
ALTER TABLE "group"
    ADD CONSTRAINT FK_GROUP_ON_COHORT FOREIGN KEY (cohort_id) REFERENCES cohort (id);
 
ALTER TABLE teacher
    ADD CONSTRAINT FK_TEACHER_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);
 
ALTER TABLE student
    ADD CONSTRAINT FK_STUDENT_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);
 
ALTER TABLE student
    ADD CONSTRAINT FK_STUDENT_ON_COHORT FOREIGN KEY (cohort_id) REFERENCES cohort (id);
 
ALTER TABLE student_group_history
    ADD CONSTRAINT FK_SGH_ON_STUDENT FOREIGN KEY (student_id) REFERENCES student (id);
 
ALTER TABLE student_group_history
    ADD CONSTRAINT FK_SGH_ON_GROUP FOREIGN KEY (group_id) REFERENCES "group" (id);
 
ALTER TABLE track_course
    ADD CONSTRAINT FK_TC_ON_TRACK FOREIGN KEY (track_id) REFERENCES track (id);
 
ALTER TABLE track_course
    ADD CONSTRAINT FK_TC_ON_COURSE FOREIGN KEY (course_id) REFERENCES course (id);
 
ALTER TABLE course_assignment
    ADD CONSTRAINT FK_CA_ON_COURSE FOREIGN KEY (course_id) REFERENCES course (id);
 
ALTER TABLE course_assignment_teaching
    ADD CONSTRAINT FK_CAT_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES course_assignment (id);
 
ALTER TABLE course_assignment_teaching
    ADD CONSTRAINT FK_CAT_ON_TEACHER FOREIGN KEY (teacher_id) REFERENCES teacher (id);
 
ALTER TABLE course_assignment_teaching
    ADD CONSTRAINT FK_CAT_ON_GROUP FOREIGN KEY (group_id) REFERENCES "group" (id);
 
ALTER TABLE exam
    ADD CONSTRAINT FK_EXAM_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES course_assignment (id);
 
ALTER TABLE grade
    ADD CONSTRAINT FK_GRADE_ON_STUDENT FOREIGN KEY (student_id) REFERENCES student (id);
 
ALTER TABLE grade
    ADD CONSTRAINT FK_GRADE_ON_EXAM FOREIGN KEY (exam_id) REFERENCES exam (id);
 
ALTER TABLE grade_history
    ADD CONSTRAINT FK_GH_ON_GRADE FOREIGN KEY (grade_id) REFERENCES grade (id);
 
ALTER TABLE transcript_delivery
    ADD CONSTRAINT FK_TD_ON_STUDENT FOREIGN KEY (student_id) REFERENCES student (id);
