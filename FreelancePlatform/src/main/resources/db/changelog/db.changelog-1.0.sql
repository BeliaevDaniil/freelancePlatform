CREATE TABLE correction
(
    id      INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    task_id INTEGER                                  NOT NULL,
    content VARCHAR(255)                             NOT NULL,
    date    TIMESTAMP WITHOUT TIME ZONE              NOT NULL,
    CONSTRAINT pk_correction PRIMARY KEY (id)
);

CREATE TABLE feedback
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    from_user_id INTEGER                                  NOT NULL,
    to_user_id   INTEGER                                  NOT NULL,
    rating       INTEGER                                  NOT NULL,
    comment      VARCHAR(255),
    CONSTRAINT pk_feedback PRIMARY KEY (id)
);

CREATE TABLE proposal
(
    id            INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    freelancer_id INTEGER                                  NOT NULL,
    task_id       INTEGER                                  NOT NULL,
    CONSTRAINT pk_proposal PRIMARY KEY (id)
);

CREATE TABLE resume
(
    id       INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    filename VARCHAR(255)                             NOT NULL,
    content  BYTEA,
    user_id  INTEGER,
    CONSTRAINT pk_resume PRIMARY KEY (id)
);

CREATE TABLE solution
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    task_id     INTEGER                                          ,
    description TEXT,
    link        TEXT                                     NOT NULL,
    CONSTRAINT pk_solution PRIMARY KEY (id)
);

CREATE TABLE task
(
    id             INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    customer_id    INTEGER                                  NOT NULL,
    freelancer_id  INTEGER,
    title          VARCHAR(255)                             NOT NULL,
    problem        VARCHAR(255)                             NOT NULL,
    deadline       TIMESTAMP WITHOUT TIME ZONE              NOT NULL,
    status         VARCHAR(255)                             NOT NULL,
    type           VARCHAR(255)                             NOT NULL,
    payment        DOUBLE PRECISION                         NOT NULL,
    assigned_date  TIMESTAMP WITHOUT TIME ZONE,
    submitted_date TIMESTAMP WITHOUT TIME ZONE,
    posted_date    TIMESTAMP WITHOUT TIME ZONE,
    solution_id    INTEGER,
    CONSTRAINT pk_task PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    username   VARCHAR(255)                             NOT NULL UNIQUE NOT NULL,
    first_name VARCHAR(255)                             NOT NULL,
    last_name  VARCHAR(255)                             NOT NULL,
    email      VARCHAR(255)                             NOT NULL,
    password   VARCHAR(255)                             NOT NULL,
    rating     INTEGER,
    role       VARCHAR(255)                             NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE resume
    ADD CONSTRAINT uc_resume_user UNIQUE (user_id);

ALTER TABLE task
    ADD CONSTRAINT uc_task_solution UNIQUE (solution_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE correction
    ADD CONSTRAINT FK_CORRECTION_ON_TASK FOREIGN KEY (task_id) REFERENCES task (id);

ALTER TABLE feedback
    ADD CONSTRAINT FK_FEEDBACK_ON_FROM_USER FOREIGN KEY (from_user_id) REFERENCES users (id);

ALTER TABLE feedback
    ADD CONSTRAINT FK_FEEDBACK_ON_TO_USER FOREIGN KEY (to_user_id) REFERENCES users (id);

ALTER TABLE proposal
    ADD CONSTRAINT FK_PROPOSAL_ON_FREELANCER FOREIGN KEY (freelancer_id) REFERENCES users (id);

ALTER TABLE proposal
    ADD CONSTRAINT FK_PROPOSAL_ON_TASK FOREIGN KEY (task_id) REFERENCES task (id);

ALTER TABLE resume
    ADD CONSTRAINT FK_RESUME_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE task
    ADD CONSTRAINT FK_TASK_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES users (id);

ALTER TABLE task
    ADD CONSTRAINT FK_TASK_ON_FREELANCER FOREIGN KEY (freelancer_id) REFERENCES users (id);

ALTER TABLE solution
    ADD CONSTRAINT FK_SOLUTION_ON_TASK FOREIGN KEY (task_id) REFERENCES task (id);